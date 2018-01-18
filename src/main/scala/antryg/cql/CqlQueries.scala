package antryg.cql

import com.datastax.driver.core.querybuilder.{Insert, QueryBuilder}

object CqlQueries {

  def insert(keyspace: String, table: String, values: Map[String, Any]): Insert = {
    var insert = QueryBuilder.insertInto(keyspace, table)
    for ((key, value) <- values) {
      insert = insert.value(key, value.asInstanceOf[AnyRef])
    }
    insert
  }

}
