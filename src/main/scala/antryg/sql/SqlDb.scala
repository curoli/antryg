package antryg.sql

import scalikejdbc.{ConnectionPool, DB, HasExtractor, NamedDB, SQL, SQLToResult, WrappedResultSet}

import scala.language.higherKinds

sealed trait SqlDb {

  def queryReadOnly[A, C[_]](query: SQLToResult[A, HasExtractor, C]): C[A]

  def queryReadOnlyForeach[A](query: SQL[A, HasExtractor], visitor: WrappedResultSet => Unit): Unit

  def readTableSchema(tableName: String): SqlTableSchema = {
    val cols = queryReadOnly(SqlQueries.describeTable(tableName))
    SqlTableSchema(tableName, cols)
  }

  def close(): Unit

}

object SqlDb {

  object DefaultDb extends SqlDb {
    SqlConnectionPools.init()

    override def queryReadOnly[A, C[_]](query: SQLToResult[A, HasExtractor, C]): C[A] =
      DB.readOnly { implicit session => query.apply() }

    override def queryReadOnlyForeach[A](query: SQL[A, HasExtractor], visitor: WrappedResultSet => Unit): Unit =
      DB.readOnly { implicit session => query.foreach(visitor) }

    override def close(): Unit = ConnectionPool.close()
  }

  case class NamedDb(name: String) extends SqlDb {
    SqlConnectionPools.init()

    override def queryReadOnly[A, C[_]](query: SQLToResult[A, HasExtractor, C]): C[A] =
      NamedDB(name).readOnly { implicit session => query.apply() }

    override def queryReadOnlyForeach[A](query: SQL[A, HasExtractor], visitor: WrappedResultSet => Unit): Unit =
      NamedDB(name).readOnly { implicit session => query.foreach(visitor) }

    override def close(): Unit = ConnectionPool.close(name)

  }

}

