package antryg.cql.builder

import antryg.cql.CqlCol
import com.datastax.driver.core.schemabuilder.{SchemaBuilder, SchemaStatement}

case class AlterTableAddCol(keyspace: String, table: String, col: CqlCol) extends AlterTable {
  override def asJava: SchemaStatement = {
    SchemaBuilder.alterTable(keyspace, table).addColumn(col.name).`type`(col.dataType)
  }

  override def colName: String = col.name
}
