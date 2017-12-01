package antryg.sql

import scalikejdbc.{DB, HasExtractor, NamedDB, SQLToList, SQLToOption, scalikejdbcSQLInterpolationImplicitDef}

object SqlQueries {

  val versionQuery: SQLToOption[String, HasExtractor] = sql"select version()".map(_.string(1)).single()

  def getVersion: String = DB.readOnly { implicit session => versionQuery.apply() }.get

  def getVersion(dbName: String): String = NamedDB(dbName).readOnly { implicit session => versionQuery.apply() }.get

  val showTablesQuery: SQLToList[String, HasExtractor] = sql"show tables".map(_.string(1)).list

  def showTables: Set[String] = DB.readOnly { implicit session => showTablesQuery.apply() }.toSet

  def showTables(dbName: String): Set[String] =
    NamedDB(dbName).readOnly { implicit session => showTablesQuery.apply() }.toSet

}
