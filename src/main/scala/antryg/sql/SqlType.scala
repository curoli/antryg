package antryg.sql

import java.util.regex.Pattern

trait SqlType {
  def typeName: String

  override def toString: String = typeName
}

trait FixedSizeType extends SqlType {
  def size: Int

  def baseName: String

  override def typeName: String = s"$baseName($size)"
}

trait FixedSizeCompanion[T <: FixedSizeType] {
  val baseName: String
  val regex: String = s"$baseName(\\d+)"

  def matches(string: String): Boolean = Pattern.matches(regex, string)

  def parseSize(string: String): Int = string.replace(s"baseName(", "").replace(")", "").toInt

  def parse(string: String): T
}

case class VarCharType(size: Int) extends FixedSizeType {
  override val baseName: String = VarCharType.baseName
}

object VarCharType extends FixedSizeCompanion[VarCharType] {
  val baseName: String = "varchar"

  override def parse(string: String): VarCharType = VarCharType(parseSize(string))
}

case class FixedSizeIntType(size: Int) extends FixedSizeType {
  override val baseName: String = "int"
}

object FixedSizeIntType extends FixedSizeCompanion[FixedSizeIntType] {
  val baseName: String = "int"

  override def parse(string: String): FixedSizeIntType = FixedSizeIntType(parseSize(string))
}

case class PrimitiveType(typeName: String) extends SqlType

object PrimitiveType {
  val float: PrimitiveType = PrimitiveType("double")
  val double: PrimitiveType = PrimitiveType("double")
}

object SqlType {

  def findKnownType(typeName: String): Option[SqlType] = {
    if (VarCharType.matches(typeName)) {
      Some(VarCharType.parse(typeName))
    } else if (FixedSizeIntType.matches(typeName)) {
      Some(FixedSizeIntType.parse(typeName))
    } else {
      ???
    }
  }

//  val knownTypes: Set[SqlType] =
//    Set("text", "double", "int(11)", "longtext", "varchar(100)", "varchar(10)", "varchar(20)", "varchar(200)",
//      "int(1)", "varchar(16)", "varchar(13)", "varchar(2)", "varchar(1)", "float", "varchar(4)", "varchar(105)",
//      "varchar(12)", "varchar(11)", "varchar(97)")
//      .map(SqlType(_))

}
