package antryg.portal.sql

import antryg.sql.{SqlCol, SqlTableSchema}

object PortalSqlSchema {

  val samplesTable = "SAMPLE"
  val variantMainTable = "VARIANT"
  val variantColMainTable = "ID"

  val variantColOtherTables = "VAR_ID"

  def getVariantColumn(schema: SqlTableSchema): Option[SqlCol] = {
    if(schema.name == variantMainTable) {
      schema.cols.find(col => col.name == variantColMainTable || col.name == variantColOtherTables)
    } else {
      schema.cols.find(col => col.name == variantColOtherTables)
    }
  }

}