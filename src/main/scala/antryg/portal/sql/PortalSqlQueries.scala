package antryg.portal.sql

import antryg.portal.cql.VariantFinderFacade.VariantCoreData
import antryg.portal.sql.PortalSqlSchema.{CommonCols, VariantMainTable}
import scalikejdbc.interpolation.SQLSyntax
import scalikejdbc.{HasExtractor, SQLToOption, SQLToTraversable, scalikejdbcSQLInterpolationImplicitDef}

object PortalSqlQueries {

  val fetchSize: Int = 1000

  def selectVariantCoreData(limit: Option[Int] = None):
  SQLToTraversable[VariantCoreData, HasExtractor] = {
    val tableToken = SQLSyntax.createUnsafely(VariantMainTable.name)
    import VariantMainTable.Cols
    val colNames = Cols.coreCols
    val colListToken = SQLSyntax.createUnsafely(colNames.mkString(", "))
    val sql = limit match {
      case Some(nLimit) => sql"select $colListToken from $tableToken limit $nLimit"
      case None => sql"select $colListToken from $tableToken"
    }
    sql.fetchSize(fetchSize).map { row =>
      VariantCoreData(row.string(Cols.variantId), row.string(Cols.chromosome), row.long(Cols.position))
    }.traversable()
  }

  def selectMetaDataVersions: SQLToTraversable[String, HasExtractor] = {
    val sql =
      sql"select distinct ver from META_MDV;"
    sql.fetchSize(fetchSize).map(row => row.string("ver")).traversable()
  }

  case class CohortPhenoTableInfo(tableName: String, cohort: String, pheno: String)

  def selectCohortPhenoByVersion(metaDataVersion: String): SQLToTraversable[CohortPhenoTableInfo, HasExtractor] = {
    val sql =
      sql"""select distinct ver.ID, dataset_ph.DATASET, dataset_ph.TBL, dataset_ph.PH
            from META_ID_PH id_ph, META_MDV ver, META_DATASET_PH dataset_ph, META_DATASET dataset, META_PH ph
            where ver.ID = id_ph.ID and id_ph.PH = dataset_ph.PH and ver.DATASET = dataset_ph.DATASET
            and ver.DATASET = dataset.DATASET and dataset_ph.PH = ph.PH
            and ver.ver = ${metaDataVersion};"""
    sql.fetchSize(fetchSize).
      map(row => CohortPhenoTableInfo(row.string("TBL"), row.string("DATASET"), row.string("PH"))).traversable()
  }

  def selectCohortPhenoByTable(table: String): SQLToOption[CohortPhenoTableInfo, HasExtractor] = {
    val sql =
      sql"""select * from META_DATASET_PH where TBL = ${table}"""
    sql.fetchSize(fetchSize).
      map(row => CohortPhenoTableInfo(row.string("TBL"), row.string("DATASET"), row.string("PH"))).headOption()
  }

  case class CohortPhenoRow(variantId: String, values: Map[String, Double])

  def asDoubleOption(any: Any): Option[Double] = {
    any match {
      case number: Double | Float | Int | Long | Char | Short | Byte => Some(number.doubleValue())
      case _ => None
    }
  }

  def selectFromCohortPhenoTable(table: String): SQLToTraversable[CohortPhenoRow, HasExtractor] = {
    val tableToken = SQLSyntax.createUnsafely(table)
    val sql = sql"select * from $tableToken"
    sql.fetchSize(fetchSize).map { row =>
      val variantId = row.string(CommonCols.variantId)
      val values = row.toMap().mapValues(asDoubleOption).collect {
        case (key, Some(value)) => (key, value)
      }
      CohortPhenoRow(variantId, values)
    }
  }

}
