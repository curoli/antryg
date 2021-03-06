package antryg.cql.facade

import antryg.cql.CqlTableSchema.PrimaryKey
import antryg.cql.builder.{AlterTableAddCol, CreateTable, DropTable, Insert, Select}
import antryg.cql.{CqlCol, CqlSession, CqlTableSchema}
import com.datastax.driver.core.ResultSet

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

  def addColIfNeeded(col: CqlCol): Unit = if (!schema.hasCol(col.name)) {
    addCol(col)
  }

  def addColsAsNeeded(cols: Iterable[CqlCol]): Unit = cols.foreach(addColIfNeeded)

  def insert(values: Map[String, Any]): Unit = {
    session.execute(Insert(keyspace.name, name, values))
  }

  def select(selectedCols: Select.SelectedCols, clauses: Seq[Select.Clause]): ResultSet = {
    session.execute(Select(keyspace.name, name, selectedCols, clauses))
  }

  def drop(): Unit = {
    session.execute(DropTable(keyspace.name, name, ifExist = true))
    exists = false
  }

  def dropIfNeeded(): Unit = if (exists) drop()

}

object CqlTableFacade {
  def apply(keyspace: KeyspaceFacade, schema: CqlTableSchema): CqlTableFacade =
    new CqlTableFacade(keyspace, schema.name, schema.key, schema.otherCols)
}
