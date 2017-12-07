package antryg.portal

import antryg.sql.{SqlConnectionPools, SqlDb, SqlQueries}
import org.scalatest.FunSuite

class SamplesTest extends FunSuite {

  test("Access samples table") {
    SqlConnectionPools.init()
    val tables = SqlDb.DefaultDb.queryReadOnly(SqlQueries.showTables)
    assert(tables.contains(PortalDbSchema.samplesTable))
    val des = SqlDb.DefaultDb.queryReadOnly(SqlQueries.describeTable(PortalDbSchema.samplesTable))
    println(des.mkString(", "))
  }

}
