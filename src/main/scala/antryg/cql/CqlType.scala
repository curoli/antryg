package antryg.cql

case class CqlType(name: String) {

}

object CqlType {
  val tinyint = CqlType("tinyint")
  val smallint = CqlType("smallint")
  val int = CqlType("int")
  val bigint = CqlType("bigint")
  val varint = CqlType("varint")
}
