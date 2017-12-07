package antryg.portal

import antryg.sql.{SqlConnectionPools, SqlDb, SqlQueries}
import antryg.sqltocql.SqlToCql
import org.scalatest.FunSuite

class SamplesTest extends FunSuite {

  test("Access samples table") {
    SqlConnectionPools.init()
    val sqlDb = SqlDb.DefaultDb
    val tables = sqlDb.queryReadOnly(SqlQueries.showTables)
    assert(tables.contains(PortalDbSchema.samplesTable))
    val schema = sqlDb.readTableSchema(PortalDbSchema.samplesTable)
    println(schema.cols.mkString(", "))
    val cqlTypes = schema.cols.map(_.sqlType).map(SqlToCql.convert)
    println(cqlTypes.mkString(", "))
  }

}
