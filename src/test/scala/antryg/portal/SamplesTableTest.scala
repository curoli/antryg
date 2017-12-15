package antryg.portal

import antryg.cql.CqlSessionFactory
import antryg.sql.{SqlConnectionPools, SqlDb, SqlQueries}
import antryg.sqltocql.SqlToCql
import com.datastax.driver.core.schemabuilder.SchemaBuilder
import org.scalatest.FunSuite

import scala.util.Random

class SamplesTableTest extends FunSuite {

  test("Copy samples table") {
    SqlConnectionPools.init()
    val sqlDb = SqlDb.DefaultDb
    val tables = sqlDb.queryReadOnly(SqlQueries.showTables)
    assert(tables.contains(PortalDbSchema.samplesTable))
    val sqlSchema = sqlDb.readTableSchema(PortalDbSchema.samplesTable)
    //    println(sqlSchema.cols.mkString(", "))
    val cqlTypes = sqlSchema.cols.map(_.sqlType).map(SqlToCql.TypeConverters.default)
    assert(cqlTypes.size > 10)
    //    println(cqlTypes.mkString(", "))
    val session = CqlSessionFactory.LocalFactory.session
    val keyspace = s"keyspace${Random.nextLong()}"
    println(s"Going to create keyspace $keyspace")
    val createKeyspaceStmt = SchemaBuilder.createKeyspace(keyspace).ifNotExists().`with`()
    println(createKeyspaceStmt.getQueryString())
    val createKeyspaceResult = session.execute(createKeyspaceStmt).one()
    println(createKeyspaceResult)
    val dropKeyspaceStmt = SchemaBuilder.dropKeyspace(keyspace)
    val dropKeyspaceResult = session.execute(dropKeyspaceStmt)
    println(dropKeyspaceResult)
  }
}
