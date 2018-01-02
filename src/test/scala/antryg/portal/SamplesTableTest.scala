package antryg.portal

import antryg.cql.builder.{CreateKeyspace, Replication}
import antryg.cql.{CqlQueries, CqlSessionFactory}
import antryg.sql.{SqlConnectionPools, SqlDb, SqlQueries, SqlTableSchema}
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
    val keyspace = s"keyspace${Random.alphanumeric.take(10).mkString("")}"
    println(s"Going to create keyspace $keyspace")
    val createKeyspaceStmt =
      CreateKeyspace(name=keyspace, ifNotExists = true, replication = Replication.SimpleStrategy(1)).asJava
    println(createKeyspaceStmt.getQueryString())
    val createKeyspaceResult = session.execute(createKeyspaceStmt).one()
    println(createKeyspaceResult)
    val samplesTableSqlCols = sqlDb.queryReadOnly(SqlQueries.describeTable(PortalDbSchema.samplesTable))
    val samplesSqlSchema = SqlTableSchema(PortalDbSchema.samplesTable, samplesTableSqlCols)
    println(samplesSqlSchema)
    val cqlTableSchema =
      SqlToCql.convertTable(samplesSqlSchema, SqlToCql.TypeConverters.defaultWithTextFallback, Seq("ID"), Seq())
    val createTableStmt = CqlQueries.createTable(keyspace, cqlTableSchema)
    println(createTableStmt)
    val createTableResult = session.execute(createTableStmt)
    println(createTableResult)
    Thread.sleep(1000)
    val sqlTableResult = sqlDb.queryReadOnly(SqlQueries.selectAll(PortalDbSchema.samplesTable, Some(100)))
    for(values <- sqlTableResult) {
      val insertStmt = CqlQueries.insert(keyspace, PortalDbSchema.samplesTable, values)
      session.execute(insertStmt)
    }
    val dropKeyspaceStmt = SchemaBuilder.dropKeyspace(keyspace)
    println(dropKeyspaceStmt.ifExists().getQueryString())
    val dropKeyspaceResult = session.execute(dropKeyspaceStmt)
    println(dropKeyspaceResult)
  }
}
