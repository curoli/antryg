package antryg.cql.builder

import antryg.cql.CqlTableSchema
import com.datastax.driver.core.schemabuilder.{Create, SchemaBuilder}

case class CreateTable(keyspace: String, schema: CqlTableSchema) extends CqlStatement {
  override def asJava: Create = {
    var createTableStmt: Create = SchemaBuilder.createTable(keyspace, schema.name)
    for(partitionCol <- schema.key.partitionCols) {
      createTableStmt = createTableStmt.addPartitionKey(partitionCol.name, partitionCol.dataType)
    }
    for(clusterCol <- schema.key.clusterCols) {
      createTableStmt = createTableStmt.addClusteringColumn(clusterCol.name, clusterCol.dataType)
    }
    for(otherCol <- schema.otherCols) {
      createTableStmt = createTableStmt.addColumn(otherCol.name, otherCol.dataType)
    }
    createTableStmt
  }
}
