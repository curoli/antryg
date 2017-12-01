package antryg.sql

import org.scalatest.FunSuite
import scalikejdbc.interpolation.SQLSyntax
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

class SqlTest extends FunSuite {

  test("Connect to default DB") {
    SqlConnectionPools.init()
    println(s"This is MySQL ${SqlQueries.getVersion}")
    val tables = SqlQueries.showTables
    //    println(s"${tables.size} tables: ${tables.mkString(", ")}.")
    val colReports = tables.map { table =>
      val cols = DB.readOnly { implicit session =>
        sql"describe ${SQLSyntax.createUnsafely(table)}".map(rs =>
          s"${rs.string("Field")} (${rs.string("Type")})"
        ).list.apply
      }
      s"$table (${cols.size} cols): ${cols.mkString(", ")}"
    }
    val report = s"${tables.size} tables: ${colReports.mkString("\n", "\n", "\n")}."
    //    println(report)
  }

}
