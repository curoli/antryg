package antryg.cql.builder
import com.datastax.driver.core.schemabuilder.{KeyspaceOptions, SchemaBuilder}
import scala.collection.JavaConverters.mapAsJavaMapConverter

case class CreateKeyspace(name: String, ifNotExists: Boolean = false, replication: Replication)
  extends CqlStatement {
  override def asJava: KeyspaceOptions = {
    val createKeyspace = SchemaBuilder.createKeyspace(name)
    val createKeyspace2 = if(ifNotExists) { createKeyspace.ifNotExists() } else createKeyspace
    createKeyspace2.`with`().
      replication(replication.asPropertyMap.mapValues(_.asInstanceOf[AnyRef]).view.force.asJava)
  }
}
