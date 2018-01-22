package antryg.sql

import scala.util.Try

case class SqlType(name: String) {
  override def toString: String = name

  val varSizeParseOpt: Option[(String, Int)] = if (name.indexOf(")") == name.length - 1) {
    val iParen = name.indexOf("(")
    val iParenCheck = name.lastIndexOf("(")
    if (iParen > 0 && iParen < name.length - 2 && iParen == iParenCheck) {
      Try {
        val baseName = name.substring(0, iParen)
        val size = name.substring(iParen + 1, name.length - 1).toInt
        if (size > 0) {
          Some((baseName, size))
        } else {
          None
        }
      }.toOption.flatten
    } else {
      None
    }
  } else {
    None
  }

  def isVarSizeType: Boolean = varSizeParseOpt.nonEmpty

  def varSizeBaseNameOpt: Option[String] = varSizeParseOpt.map(_._1)

  def varSizeOpt: Option[Int] = varSizeParseOpt.map(_._2)
}


object SqlType {
  val text = SqlType("text")
  val longtext = SqlType("longtext")
  val float = SqlType("float")
  val double = SqlType("double")
  val bigint = SqlType("bigint")
  val datetime = SqlType("datetime")
  val timestamp = SqlType("timestamp")
  object VarSizeTypes {
    val int = "int"
    val varchar = "varchar"
    val bigint = "bigint"
  }

}
