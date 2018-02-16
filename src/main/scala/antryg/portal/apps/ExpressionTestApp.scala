package antryg.portal.apps

import antryg.expressions.BinaryExpression.NumericalComparisonExpression
import antryg.expressions.BinaryOperator.NumericalComparisonOperator
import antryg.expressions.numeric.NumericConstant

object ExpressionTestApp extends App {

  val one = NumericConstant(1.0)
  val opLessThan = NumericalComparisonOperator.lessThan
  val two = NumericConstant(2.0)
  val oneLessThanTwo = NumericalComparisonExpression(one, opLessThan, two)
  println(oneLessThanTwo)
  println(oneLessThanTwo.valueOpt)
  println(opLessThan(1.0, 2.0))

}
