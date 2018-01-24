package antryg.sql

import scalikejdbc.{DB, HasExtractor, NamedDB, SQLToResult}
import scala.language.higherKinds

sealed trait SqlDb {

  def queryReadOnly[A, C[_]](query: SQLToResult[A, HasExtractor, C]): C[A]

  def readTableSchema(tableName: String): SqlTableSchema = {
    val cols = queryReadOnly(SqlQueries.describeTable(tableName))
    SqlTableSchema(tableName, cols)
  }

}

object SqlDb {

  object DefaultDb extends SqlDb {
    SqlConnectionPools.init()
    override def queryReadOnly[A, C[_]](query: SQLToResult[A, HasExtractor, C]): C[A] =
      DB.readOnly { implicit session => query.apply() }
  }

  case class NamedDb(name: String) extends SqlDb {
    SqlConnectionPools.init()
    override def queryReadOnly[A, C[_]](query: SQLToResult[A, HasExtractor, C]): C[A] =
      NamedDB(name).readOnly { implicit session => query.apply() }
  }

}

