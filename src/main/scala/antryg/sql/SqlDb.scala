package antryg.sql

import scalikejdbc.{ConnectionPool, DB, HasExtractor, NamedDB, SQL, SQLToResult, WrappedResultSet}

import scala.language.higherKinds

sealed trait SqlDb {

  var fetchSizeOpt: Option[Int] = None

  def queryReadOnly[A, C[_]](query: SQLToResult[A, HasExtractor, C]): C[A]

  def queryReadOnlyForeach[A](query: SQL[A, HasExtractor], visitor: WrappedResultSet => Unit): Unit

  def readTableSchema(tableName: String): SqlTableSchema = {
    val cols = queryReadOnly(SqlQueries.describeTable(tableName))
    SqlTableSchema(tableName, cols)
  }

  def setFetchSize(fetchSize: Int): Unit = {
    fetchSizeOpt = Some(fetchSize)
  }

  def close(): Unit

}

object SqlDb {

  object DefaultDb extends SqlDb {
    SqlConnectionPools.init()

    override def queryReadOnly[A, C[_]](query: SQLToResult[A, HasExtractor, C]): C[A] =
      DB.readOnly { implicit session =>
        fetchSizeOpt.foreach(session.fetchSize)
        query.apply()
      }

    override def queryReadOnlyForeach[A](query: SQL[A, HasExtractor], visitor: WrappedResultSet => Unit): Unit =
      DB.readOnly { implicit session =>
        fetchSizeOpt.foreach(session.fetchSize)
        query.foreach(visitor)
      }

    override def close(): Unit = ConnectionPool.close()
  }

  case class NamedDb(name: String) extends SqlDb {
    SqlConnectionPools.init()

    override def queryReadOnly[A, C[_]](query: SQLToResult[A, HasExtractor, C]): C[A] =
      NamedDB(name).readOnly { implicit session =>
        fetchSizeOpt.foreach(session.fetchSize)
        query.apply()
      }

    override def queryReadOnlyForeach[A](query: SQL[A, HasExtractor], visitor: WrappedResultSet => Unit): Unit =
      NamedDB(name).readOnly { implicit session =>
        fetchSizeOpt.foreach(session.fetchSize)
        query.foreach(visitor)
      }

    override def close(): Unit = ConnectionPool.close(name)

  }

}

