package antryg.portal

import antryg.sql.{SqlConnectionPools, SqlQueries, SqlUtils}
import org.scalatest.FunSuite

class SamplesTest extends FunSuite {

  test("Access samples table") {
    SqlConnectionPools.init()
    val tables = SqlUtils.withDefaultDB(SqlQueries.showTables)
    assert(tables.contains(PortalDbSchema.samplesTable))
    val des = SqlUtils.withDefaultDB(SqlQueries.describeTable(PortalDbSchema.samplesTable))
    println(des.mkString(", "))
  }

}
