package antryg.cql

import antryg.cql.CqlTableSchema.PrimaryKey

case class CqlTableSchema(name: String, key: PrimaryKey, otherCols: Seq[CqlCol]) {

}

object CqlTableSchema {
  case class PrimaryKey(partitionCols: Seq[CqlCol], clusterCols: Seq[CqlCol])
}