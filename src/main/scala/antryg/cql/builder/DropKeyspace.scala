package antryg.cql.builder
import com.datastax.driver.core.RegularStatement
import com.datastax.driver.core.schemabuilder.SchemaBuilder

case class DropKeyspace(name: String, ifExist: Boolean = false) extends CqlStatement {
  override def asJava: RegularStatement = {
    val dropKeyspaceStmtPre = SchemaBuilder.dropKeyspace(name)
    val dropKeyspaceStmt = if(ifExist) { dropKeyspaceStmtPre.ifExists() } else { dropKeyspaceStmtPre }
    dropKeyspaceStmt
  }
}
