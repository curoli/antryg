package antryg.expressions.parse

import antryg.expressions.parse.ExpressionParser.{ParseGotLogicalExpression, ParseGotNumericalExpression, ParseResult}
import org.scalatest.FunSuite

class ExpressionParserTest extends FunSuite {

  def resultMessage(string: String, result: ParseResult): String = s"'$string' lead to $result"

  def assertParsedNumericalExpression(parser: ExpressionParser, string: String): Unit = {
    val result = parser.parse(string)
    assert(result.isInstanceOf[ParseGotNumericalExpression], resultMessage(string, result))
  }

  def assertParsedLogicalExpression(parser: ExpressionParser, string: String): Unit = {
    val result = parser.parse(string)
    assert(result.isInstanceOf[ParseGotLogicalExpression], resultMessage(string, result))
  }

  test("parse") {
    val symbols = ExpressionSymbols.default
    val parser = ExpressionParser(symbols)
    val numericalExpressionStrings =
      Seq("1", "x", "x42", "1+y", "(1+x)*(1-y)", "9*x*x + 6*x + 1", "-1", "+1", "(2)", "1++2")
    numericalExpressionStrings.foreach(assertParsedNumericalExpression(parser, _))
    val logicalExpressionStrings =
      Seq("a < b & b < c", "x+y = z", "x1 <= x2 & x1 >= x3")
    logicalExpressionStrings.foreach(assertParsedLogicalExpression(parser, _))
  }

}
