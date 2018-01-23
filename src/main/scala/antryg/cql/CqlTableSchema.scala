package antryg.cql

import antryg.cql.CqlTableSchema.PrimaryKey

case class CqlTableSchema(name: String, key: PrimaryKey, otherCols: Seq[CqlCol]) {

  def hasCol(name: String): Boolean =
    key.partitionCols.exists(_.name == name) || key.clusterCols.exists(_.name == name) ||
      otherCols.exists(_.name == name)

  def :+(col: CqlCol): CqlTableSchema = copy(otherCols = otherCols :+ col)

}

object CqlTableSchema {

  case class PrimaryKey(partitionCols: Seq[CqlCol], clusterCols: Seq[CqlCol])

}