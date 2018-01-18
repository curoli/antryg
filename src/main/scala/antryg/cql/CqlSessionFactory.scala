package antryg.cql

import com.datastax.driver.core.{Cluster, Session => JSession}

trait CqlSessionFactory {
  val session: CqlSession
}

object CqlSessionFactory {
  object LocalFactory extends CqlSessionFactory {
    val address: String = "127.0.0.1"
    val jSession: JSession = Cluster.builder().addContactPoint("").build().connect()
    override val session: CqlSession = CqlSession(address, jSession)
  }
}
