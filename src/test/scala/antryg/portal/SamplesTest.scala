package antryg.portal

import antryg.portal.SamplesTest.UnconvertedType
import antryg.sql.{SqlCol, SqlConnectionPools, SqlDb, SqlQueries, SqlType}
import antryg.sqltocql.SqlToCql
import org.scalatest.FunSuite

class SamplesTest extends FunSuite {

  test("Access samples table") {
    SqlConnectionPools.init()
    val sqlDb = SqlDb.DefaultDb
    val tables = sqlDb.queryReadOnly(SqlQueries.showTables)
    var unconvertedTypes: Seq[UnconvertedType] = Seq.empty
    for (table <- tables) {
      val schema = sqlDb.readTableSchema(table)
      for (col <- schema.cols) {
        val sqlType = col.sqlType
        SqlToCql.TypeConverters.default.lift(sqlType) match {
          case Some(_) => ()
          case None =>
            unconvertedTypes :+= UnconvertedType(table, col.name, sqlType)
        }
      }
    }
    println(unconvertedTypes.mkString("\n"))
    assert(tables.contains(PortalDbSchema.samplesTable))
    val schema = sqlDb.readTableSchema(PortalDbSchema.samplesTable)
    println(schema.cols.mkString(", "))
    val cqlTypes = schema.cols.map(_.sqlType).map(SqlToCql.TypeConverters.default)
    println(cqlTypes.mkString(", "))
  }

}

object SamplesTest {

  case class UnconvertedType(table: String, col: String, sqlType: SqlType)

}