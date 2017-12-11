package antryg.cql

import com.datastax.driver.core.{Cluster, Session}

trait CqlSessionFactory {
  val address: String
  val session: Session
}

object CqlSessionFactory {
  object LocalFactory extends CqlSessionFactory {
    override val address: String = "127.0.0.1"
    override val session: Session = Cluster.builder().addContactPoint("").build().connect()
  }
}
