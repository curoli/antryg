package antryg.cql.builder

import com.datastax.driver.core.schemabuilder.{Drop, SchemaBuilder}

case class DropTable(keyspace: String, name: String, ifExist: Boolean = false) extends CqlStatement {
  override def asJava: Drop = {
    val dropTableStmtPre = SchemaBuilder.dropTable(keyspace, name)
    val dropTableStmt = if (ifExist) {
      dropTableStmtPre.ifExists()
    } else {
      dropTableStmtPre
    }
    dropTableStmt
  }
}
