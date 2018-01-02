package antryg.cql.builder


trait Replication {

  def asPropertyMap: Map[String, Any]

}

object Replication {

  case class SimpleStrategy(factor: Int) extends Replication {
    override def asPropertyMap: Map[String, Any] =
      Map("class" -> "SimpleStrategy", "replication_factor" -> factor)
  }

  case class NetworkTopologyStrategy(factors: Map[String, Int]) extends Replication {
    override def asPropertyMap: Map[String, Any] = factors ++ Map("class" -> "NetworkTopologyStrategy")
  }

}