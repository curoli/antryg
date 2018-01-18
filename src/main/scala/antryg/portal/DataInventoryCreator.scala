package antryg.portal

import antryg.sql.{SqlDb, SqlQueries, SqlTableSchema}

object DataInventoryCreator {

  case class TableCategory(name: String)

  object TableCategory {
    val variants = TableCategory("variants")
    val samples = TableCategory("samples")
    val other = TableCategory("other")
    val list = List(variants, samples, other)
  }

  case class DataInventory(tableNames: Set[String], schemas: Map[String, SqlTableSchema],
                           categories: Map[String, TableCategory])

  val knownTables: Map[String, TableCategory] =
    Map(PortalDbSchema.variantTable -> TableCategory.variants, PortalDbSchema.samplesTable -> TableCategory.samples)

  def hasVariantColumn(schema: SqlTableSchema): Boolean =
    schema.cols.exists(col => col.name == PortalDbSchema.variantColumn)

  def createInventory(sqlDb: SqlDb): DataInventory = {
    val tableNames = sqlDb.queryReadOnly(SqlQueries.showTables).toSet
    val schemas = tableNames.map { tableName =>
      val schema = SqlTableSchema(tableName, sqlDb.queryReadOnly(SqlQueries.describeTable(tableName)))
      (tableName, schema)
    }.toMap
    val categories = schemas.map {
      case (tableName, schema) =>
        val category = knownTables.get(tableName).orElse {
          if (hasVariantColumn(schema)) Some(TableCategory.variants) else None
        }.getOrElse(TableCategory.other)
        (tableName, category)
    }
    DataInventory(tableNames, schemas, categories)
  }

}
