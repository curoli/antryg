package antryg.portal.sql

import antryg.sql.{SqlCol, SqlDb, SqlQueries, SqlTableSchema}

object DataInventoryCreator {

  case class TableInventory(name: String, schema: SqlTableSchema, variantIdColOpt: Option[SqlCol]) {
    val isVariantTable: Boolean = variantIdColOpt.nonEmpty
    val isMainVariantTable: Boolean = name == PortalSqlSchema.variantMainTable
    val studyAndTraitOpt: Option[(String, String)] = {
      val nameSplit = name.split("__")
      if (nameSplit.size == 2) {
        Some((nameSplit(0), nameSplit(1)))
      } else {
        None
      }
    }
    val isStudyAndTraitTable: Boolean = studyAndTraitOpt.nonEmpty
    val studyOpt: Option[String] = studyAndTraitOpt.map(_._1)
    val traitOpt: Option[String] = studyAndTraitOpt.map(_._2)
  }

  case class DataInventory(tableNames: Set[String], tableInventories: Map[String, TableInventory])

  def createInventory(sqlDb: SqlDb): DataInventory = {
    val tableNames = sqlDb.queryReadOnly(SqlQueries.showTables).toSet
    val tableInventories = tableNames.map { tableName =>
      val schema = SqlTableSchema(tableName, sqlDb.queryReadOnly(SqlQueries.describeTable(tableName)))
      val variantIdColOpt = PortalSqlSchema.getVariantColumn(schema)
      val tableInventory = TableInventory(tableName, schema, variantIdColOpt)
      (tableName, tableInventory)
    }.toMap
    DataInventory(tableNames, tableInventories)
  }

}
