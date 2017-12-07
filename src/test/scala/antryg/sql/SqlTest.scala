package antryg.sql

import org.scalatest.FunSuite

class SqlTest extends FunSuite {

  test("Connect to default DB, run sanity checks.") {
    SqlConnectionPools.init()
    val regexMatchingDot = "\\."
    val mySqlMajorVersionDetected =
      SqlDb.DefaultDb.queryReadOnly(SqlQueries.mysqlVersion).get.split(regexMatchingDot).head.toInt
    val mySqlMajorVersionMinimum = 5
    assert(mySqlMajorVersionDetected >= mySqlMajorVersionMinimum)
    val tables = SqlDb.DefaultDb.queryReadOnly(SqlQueries.showTables)
    assert(tables.nonEmpty)
    tables.foreach { table =>
      val cols = SqlDb.DefaultDb.queryReadOnly(SqlQueries.describeTable(table))
      assert(cols.nonEmpty)
    }
  }

}
