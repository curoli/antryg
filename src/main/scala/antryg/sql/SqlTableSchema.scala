package antryg.sql

case class SqlTableSchema(tableName: String, cols: Seq[SqlCol]) {
  val colsByName: Map[String, SqlCol] = cols.map(col => (col.name, col)).toMap
}
