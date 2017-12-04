package antryg.sql

import scalikejdbc.interpolation.SQLSyntax
import scalikejdbc.{HasExtractor, SQLToList, SQLToOption, scalikejdbcSQLInterpolationImplicitDef}

import scala.language.higherKinds

object SqlQueries {

  val mysqlVersion: SQLToOption[String, HasExtractor] = sql"select version()".map(_.string(1)).single()

  val showTables: SQLToList[String, HasExtractor] = sql"show tables".map(_.string(1)).list

  def describeTable(tableName: String): SQLToList[String, HasExtractor] = {
    val tableToken = SQLSyntax.createUnsafely(tableName)
    sql"describe $tableToken".map(rs =>
      s"${rs.string("Field")} (${rs.string("Type")})"
    ).list
  }

}
