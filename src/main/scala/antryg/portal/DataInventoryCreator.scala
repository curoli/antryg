package antryg.portal

import antryg.sql.{SqlCol, SqlDb, SqlQueries, SqlTableSchema}

object DataInventoryCreator {

  case class TableInventory(name: String, schema: SqlTableSchema, variantIdColOpt: Option[SqlCol]) {
    def isVariantTable: Boolean = variantIdColOpt.nonEmpty
  }

  case class DataInventory(tableNames: Set[String], tableInventories: Map[String, TableInventory])

  def createInventory(sqlDb: SqlDb): DataInventory = {
    val tableNames = sqlDb.queryReadOnly(SqlQueries.showTables).toSet
    val tableInventories = tableNames.map { tableName =>
      val schema = SqlTableSchema(tableName, sqlDb.queryReadOnly(SqlQueries.describeTable(tableName)))
      val variantIdColOpt = PortalDbSchema.getVariantColumn(schema)
      val tableInventory = TableInventory(tableName, schema, variantIdColOpt)
      (tableName, tableInventory)
    }.toMap
    DataInventory(tableNames, tableInventories)
  }

}
