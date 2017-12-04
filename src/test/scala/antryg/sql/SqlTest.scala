package antryg.sql

import org.scalatest.FunSuite
import scalikejdbc.interpolation.SQLSyntax
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

class SqlTest extends FunSuite {

  test("Connect to default DB, run sanity checks.") {
    SqlConnectionPools.init()
    val regexMatchingDot = "\\."
    val mySqlMajorVersionDetected =
      SqlUtils.withDefaultDB(SqlQueries.mysqlVersion).get.split(regexMatchingDot).head.toInt
    val mySqlMajorVersionMinimum = 5
    assert(mySqlMajorVersionDetected >= mySqlMajorVersionMinimum)
    val tables = SqlUtils.withDefaultDB(SqlQueries.showTables)
    assert(tables.nonEmpty)
    tables.foreach { table =>
      val cols = SqlUtils.withDefaultDB(SqlQueries.describeTable(table))
      assert(cols.nonEmpty)
    }
  }

}
