package antryg.cql

import com.datastax.driver.core.querybuilder.QueryBuilder
import org.scalatest.FunSuite

class QueryBuilderTest extends FunSuite {

  test("QueryBuilder") {
    val session = CqlSessionFactory.LocalFactory.jSession
    val statement = QueryBuilder.select("release_version").from("system", "local").where()
    val row = session.execute(statement).one()
    val version = row.getString("release_version")
    val dotRegex = "\\."
    val majorVersion = version.split(dotRegex).head.toInt
    assert(majorVersion >= 3)
  }

}
