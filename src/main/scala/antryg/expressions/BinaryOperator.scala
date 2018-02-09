package antryg.expressions

trait BinaryOperator[L, R, +T] extends ((L, R) => T) {
  override def apply(lhs: L, rhs: R): T

  def symbol: String

  def precedence: Int

  override def toString: String = symbol
}

object BinaryOperator {
  object Precedences {
    val timesDivideBy = 5
    val plusMinus = 4
    val andOr = 3
    val comparison = 2
  }
}