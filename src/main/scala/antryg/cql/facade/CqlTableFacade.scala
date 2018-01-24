package antryg.cql.facade

import antryg.cql.{CqlCol, CqlSession, CqlTableSchema}
import antryg.cql.CqlTableSchema.PrimaryKey
import antryg.cql.builder.{AlterTableAddCol, CreateTable, DropTable, Insert}

class CqlTableFacade(val keyspace: KeyspaceFacade, val name: String, val primaryKey: PrimaryKey,
                     otherCols: Seq[CqlCol] = Seq.empty) {

  var exists: Boolean = false

  var schema: CqlTableSchema = CqlTableSchema(name, primaryKey, otherCols)

  def session: CqlSession = keyspace.session

  def create(): Unit = {
    keyspace.createIfNeeded()
    session.execute(CreateTable(keyspace.name, schema, ifNotExists = true))
    exists = true
  }

  def createIfNeeded(): Unit = if (!exists) create()

  def addCol(col: CqlCol): Unit = {
      createIfNeeded()
      session.execute(AlterTableAddCol(keyspace.name, name, col))
      schema :+= col
  }

  def addColIfNeeded(col: CqlCol): Unit = if (!schema.hasCol(col.name)) {addCol(col) }

  def addColsAsNeeded(cols: Iterable[CqlCol]): Unit = cols.foreach(addColIfNeeded)

  def insert(values: Map[String, Any]): Unit = {
    session.execute(Insert(keyspace.name, name, values))
  }

  def drop(): Unit = {
    session.execute(DropTable(keyspace.name, name, ifExist = true))
    exists = false
  }

  def dropIfNeeded(): Unit = if (exists) drop()

}
