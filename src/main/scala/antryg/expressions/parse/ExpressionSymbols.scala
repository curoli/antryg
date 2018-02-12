package antryg.expressions.parse

import antryg.expressions.BinaryOperator

case class ExpressionSymbols(binaryOperators: Map[String, BinaryOperator.Base],
                             openBrackets: Set[String], closeBrackets: Set[String] ) {

}

object ExpressionSymbols {
  def apply(binaryOperators: Iterable[BinaryOperator.Base], openBrackets: Set[String], closeBrackets: Set[String]):
  ExpressionSymbols = {
    val binaryOperatorMap: Map[String, BinaryOperator.Base] = binaryOperators.map(op => (op.symbol, op)).toMap
    ExpressionSymbols(binaryOperatorMap, openBrackets, closeBrackets)
  }

  val default: ExpressionSymbols = {
    import BinaryOperator.ArithmeticOperator.{plus, minus, times, dividedBy}
    import BinaryOperator.NumericalComparisonOperator.{equalTo, greaterThan, greaterOrEqual, lessThan, lessOrEqual}
    import BinaryOperator.BooleanOperator.{and, or}
    val binaryOperators =
      Set(plus, minus, times, dividedBy, equalTo, greaterThan, greaterOrEqual, lessThan, lessOrEqual, and, or)
    val openBrackets = Set("(")
    val closeBrackets = Set(")")
    ExpressionSymbols(binaryOperators, openBrackets, closeBrackets)
  }
}
