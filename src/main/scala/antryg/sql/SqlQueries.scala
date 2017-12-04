package antryg.sql

import scalikejdbc.{HasExtractor, SQLToList, SQLToOption, scalikejdbcSQLInterpolationImplicitDef}

import scala.language.higherKinds

object SqlQueries {

  val mysqlVersion: SQLToOption[String, HasExtractor] = sql"select version()".map(_.string(1)).single()

  val showTables: SQLToList[String, HasExtractor] = sql"show tables".map(_.string(1)).list

}
