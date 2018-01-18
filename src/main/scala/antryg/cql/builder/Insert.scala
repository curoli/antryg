package antryg.cql.builder

import com.datastax.driver.core.querybuilder.{QueryBuilder, Insert => JInsert}

case class Insert(keyspace: String, table: String, values: Map[String, Any]) extends CqlStatement {
  override def asJava: JInsert = {
    var insert = QueryBuilder.insertInto(keyspace, table)
    for ((key, value) <- values) {
      insert = insert.value(key, value.asInstanceOf[AnyRef])
    }
    insert
  }
}
