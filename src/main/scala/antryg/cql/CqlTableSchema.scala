package antryg.cql

case class CqlTableSchema(name: String, partitionCols: Seq[CqlCol], clusterCols: Seq[CqlCol],
                          otherCols: Seq[CqlCol]) {

}
