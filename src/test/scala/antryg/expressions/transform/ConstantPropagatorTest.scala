package antryg.expressions.transform

import antryg.expressions.logical.BooleanConstant
import antryg.expressions.numeric.NumericConstant
import antryg.expressions.parse.ExpressionParser
import antryg.expressions.parse.ExpressionParser.{ParseGotLogicalExpression, ParseGotNumericalExpression}
import org.scalatest.FunSuite

class ConstantPropagatorTest extends FunSuite {

  val parser = ExpressionParser()

  def assertReducedToNumericConstant(string: String, value: Double): Unit = {
    val result = parser.parse(string)
    assert(result.isInstanceOf[ParseGotNumericalExpression])
    val expression = result.asInstanceOf[ParseGotNumericalExpression].expression
    val reducedExpression = ConstantPropagator.transformNumeric(expression)
    assert(reducedExpression === NumericConstant(value))
  }

  def assertReducedToBooleanConstant(string: String, value: Boolean): Unit = {
    val result = parser.parse(string)
    assert(result.isInstanceOf[ParseGotLogicalExpression])
    val expression = result.asInstanceOf[ParseGotLogicalExpression].expression
    val reducedExpression = ConstantPropagator.transformBoolean(expression)
    assert(reducedExpression === BooleanConstant(value))
  }

  test("constant propagation") {
    assertReducedToNumericConstant("2*3 + 2*(3+4)", 20)
    assertReducedToNumericConstant("1 + 2 + 3 + 4 + 5 + 6", 21)
    assertReducedToNumericConstant("(3+4)*(5+6)", 77)
    assertReducedToBooleanConstant("1 > 2 | 3 > 4 | 4 > 5 | 6 < 7 | 8 > 9", value = true)
    assertReducedToBooleanConstant("1 < 2 & 3 < 4 & 4 < 5 & 6 > 7 & 8 < 9", value = false)

  }

}
