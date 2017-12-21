package antryg.cql

import com.datastax.driver.core.querybuilder.{Insert, QueryBuilder}
import com.datastax.driver.core.schemabuilder.{Create, SchemaBuilder}

object CqlQueries {

  def createTable(keyspace: String, schema: CqlTableSchema): Create = {
    var createTableStmt: Create = SchemaBuilder.createTable(keyspace, schema.name)
    for(partitionCol <- schema.partitionCols) {
      createTableStmt = createTableStmt.addPartitionKey(partitionCol.name, partitionCol.dataType)
    }
    for(clusterCol <- schema.clusterCols) {
      createTableStmt = createTableStmt.addClusteringColumn(clusterCol.name, clusterCol.dataType)
    }
    for(otherCol <- schema.otherCols) {
      createTableStmt = createTableStmt.addColumn(otherCol.name, otherCol.dataType)
    }
    createTableStmt
  }

  def insert(keyspace: String, table:String, values: Map[String, Any]): Insert = {
    var insert = QueryBuilder.insertInto(keyspace, table)
    for((key, value) <- values) {
      insert = insert.value(key, value.asInstanceOf[AnyRef])
    }
    insert
  }

}
