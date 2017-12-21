package antryg.sql

import scalikejdbc.interpolation.SQLSyntax
import scalikejdbc.{HasExtractor, SQLToList, SQLToOption, SQLToTraversable, scalikejdbcSQLInterpolationImplicitDef}

import scala.language.higherKinds

object SqlQueries {

  val mysqlVersion: SQLToOption[String, HasExtractor] = sql"select version()".map(_.string(1)).single()

  val showTables: SQLToList[String, HasExtractor] = sql"show tables".map(_.string(1)).list

  def describeTable(tableName: String): SQLToList[SqlCol, HasExtractor] = {
    val tableToken = SQLSyntax.createUnsafely(tableName)
    sql"describe $tableToken".map(rs => SqlCol(rs.string("Field"), SqlType(rs.string("Type")))
    ).list
  }

  def selectAll(tableName: String, limit: Option[Int] = None): SQLToTraversable[Map[String, Any], HasExtractor] = {
    val tableToken = SQLSyntax.createUnsafely(tableName)
    val sql = limit match {
      case Some(nLimit) => sql"select * from $tableToken limit $nLimit"
      case None => sql"select * from $tableToken"
    }
    sql.map(_.toMap()).traversable()
  }

}
