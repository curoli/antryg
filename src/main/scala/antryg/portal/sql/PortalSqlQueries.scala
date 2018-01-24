package antryg.portal.sql

import antryg.portal.cql.VariantFinderFacade.VariantCoreData
import antryg.portal.sql.PortalSqlSchema.VariantMainTable
import scalikejdbc.interpolation.SQLSyntax
import scalikejdbc.{HasExtractor, SQLToTraversable, scalikejdbcSQLInterpolationImplicitDef}

object PortalSqlQueries {

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
    sql.map { row =>
      VariantCoreData(row.string(Cols.variantId), row.string(Cols.chromosome), row.long(Cols.position))
    }.traversable()
  }


}
