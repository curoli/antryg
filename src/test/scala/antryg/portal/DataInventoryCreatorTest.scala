package antryg.portal

import antryg.sql.{SqlConnectionPools, SqlDb}
import org.scalatest.FunSuite

class DataInventoryCreatorTest extends FunSuite {
  test("find") {
    SqlConnectionPools.init()
    val sqlDb = SqlDb.DefaultDb
    val inventory = DataInventoryCreator.createInventory(sqlDb)
    print(inventory.schemas.values.map { schema =>
      s"${schema.name}: ${schema.cols.map(col => s"${col.name} (${col.sqlType})").mkString(", ")}"
    }.mkString("\n", "\n", "\n"))
    val groupedByCategories = inventory.categories.groupBy(_._2)
    for(category <- DataInventoryCreator.TableCategory.list) {
      val tableNamesOfCategory = groupedByCategories.getOrElse(category, Map.empty).keySet
      println(s"${category.name}: ${tableNamesOfCategory.mkString(", ")}")
    }
  }
}
