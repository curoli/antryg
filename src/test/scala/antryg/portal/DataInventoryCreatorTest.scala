package antryg.portal

import antryg.portal.sql.DataInventoryCreator
import antryg.sql.{SqlConnectionPools, SqlDb}
import org.scalatest.FunSuite

class DataInventoryCreatorTest extends FunSuite {
  test("find") {
    SqlConnectionPools.init()
    val sqlDb = SqlDb.DefaultDb
    val inventory = DataInventoryCreator.createInventory(sqlDb)
    print(inventory.tableInventories.values.map { tableInventory =>
      val schema = tableInventory.schema
      s"${schema.name}: ${schema.cols.map(col => s"${col.name} (${col.sqlType})").mkString(", ")}"
    }.mkString("\n", "\n", "\n"))
    val tableInventories = inventory.tableInventories.values.toSet
    val variantTableInventories = tableInventories.filter(_.isVariantTable)
    val otherTableInventories = tableInventories -- variantTableInventories
    println("Tables by category:")
    println(s"Variant tables: ${variantTableInventories.map(_.name).mkString(", ")}")
    println(s"Other tables: ${otherTableInventories.map(_.name).mkString(", ")}")
  }
}
