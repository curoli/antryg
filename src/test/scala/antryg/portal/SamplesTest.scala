package antryg.portal

import antryg.sql.{SqlCol, SqlConnectionPools, SqlDb, SqlQueries}
import antryg.sqltocql.SqlToCql
import org.scalatest.FunSuite

class SamplesTest extends FunSuite {

  test("Access samples table") {
    SqlConnectionPools.init()
    val sqlDb = SqlDb.DefaultDb
    val tables = sqlDb.queryReadOnly(SqlQueries.showTables)
    for(table <- tables) {
      val schema = sqlDb.readTableSchema(table)
      def colToString(col: SqlCol): String = s"${col.name}(${col.sqlType.name}, ${SqlToCql.convert(col.sqlType).name})"
      println(s"${schema.tableName}: ${schema.cols.map(colToString).mkString(", ")}")
    }
    assert(tables.contains(PortalDbSchema.samplesTable))
    val schema = sqlDb.readTableSchema(PortalDbSchema.samplesTable)
    println(schema.cols.mkString(", "))
    val cqlTypes = schema.cols.map(_.sqlType).map(SqlToCql.convert)
    println(cqlTypes.mkString(", "))
  }

}
