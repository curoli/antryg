package antryg.portal.sqltocql

import antryg.portal.cql.VariantFinderFacade
import antryg.portal.cql.VariantFinderSchema.{Cols => CqlCols}
import antryg.portal.sql.{PortalSqlQueries, PortalSqlSchema}
import antryg.portal.sql.PortalSqlSchema.VariantMainTable.{Cols => SqlCols}
import antryg.sql.{SqlDb, SqlQueries}

class VariantFinderLoader(sqlDb: SqlDb, variantFinderFacade: VariantFinderFacade, variantIdSampler: VariantIdSampler) {

  val mainTableColsMap: Map[String, String] = Map(
    SqlCols.variantId -> CqlCols.variantId,
    SqlCols.chromosome -> CqlCols.chromosome,
    SqlCols.position -> CqlCols.position
  ).mapValues(_.name)

  def loadVariantMainTable(): Unit = {
    val sqlColNames = Seq(SqlCols.variantId, SqlCols.chromosome, SqlCols.position)
    val rows = sqlDb.queryReadOnly(PortalSqlQueries.selectVariantCoreData())
    rows.foreach { row =>
      if (variantIdSampler(row.variantId)) {
        variantFinderFacade.insertVariantCoreData(row)
      }
    }
  }

  def load(): Unit = {
    loadVariantMainTable()
  }

}
