package antryg.portal.sqltocql

import antryg.portal.cql.VariantFinderFacade
import antryg.portal.sql.PortalSqlSchema
import antryg.sql.{SqlDb, SqlQueries}

class VariantFinderLoader(sqlDb: SqlDb, variantFinderFacade: VariantFinderFacade, variantIdSampler: VariantIdSampler) {

  val variantMainTableCols: Seq[String] = Seq(PortalSqlSchema.variantMainTable)

  def loadVariantMainTable(): Unit = {
    val sqlColNames = {
      import antryg.portal.sql.PortalSqlSchema.VariantMainTable._
      Seq(variantId, chromosome, position)
    }
    val result = sqlDb.queryReadOnly(SqlQueries.select(PortalSqlSchema.VariantMainTable.name, sqlColNames))
    // TODO
  }

  def load(): Unit = {
    loadVariantMainTable()
  }

}
