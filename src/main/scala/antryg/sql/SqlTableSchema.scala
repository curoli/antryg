package antryg.sql

case class SqlTableSchema(name: String, cols: Seq[SqlCol]) {
  val colsByName: Map[String, SqlCol] = cols.map(col => (col.name, col)).toMap
}
