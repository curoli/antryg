package antryg.cql.builder

import com.datastax.driver.core.schemabuilder.SchemaStatement

trait AlterTable extends CqlStatement {

  override def asJava: SchemaStatement

  def keyspace: String

  def table: String

  def colName: String

}
