package antryg.cql.builder

import com.datastax.driver.core.querybuilder.{QueryBuilder, Insert => JInsert}
import scala.collection.JavaConverters.mapAsJavaMapConverter

case class Insert(keyspace: String, table: String, values: Map[String, Any]) extends CqlStatement {
  override def asJava: JInsert = {
    var insert = QueryBuilder.insertInto(keyspace, table)
    for ((key, value) <- values) {
      insert = insert.value(key, Insert.valueToJava(value))
    }
    insert
  }
}

object Insert {

  def valueToJava(value: Any): AnyRef = {
    val anyRef = value.asInstanceOf[AnyRef]
    anyRef match {
      case map: Map[_, _] => map.asJava
      case _ => anyRef
    }
  }

}
