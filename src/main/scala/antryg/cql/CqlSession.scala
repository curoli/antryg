package antryg.cql

import antryg.cql.builder.CqlStatement
import com.datastax.driver.core.{ResultSet, ResultSetFuture, Session}

case class CqlSession(address: String, session: Session) {
  def execute(string: String): ResultSet = session.execute(string)
  def execute(statement: CqlStatement): ResultSet = session.execute(statement.asJava)
  def executeAsync(string: String): ResultSetFuture = session.executeAsync(string)
  def executeAsync(statement: CqlStatement): ResultSetFuture = session.executeAsync(statement.asJava)
  def close(): Unit = session.close()
}
