package antryg.expressions.parse

import org.scalatest.FunSuite

class ExpressionFastParserTest extends FunSuite {

  test("parse") {
    val parser = ExpressionFastParser
//    println(parser.plusMinus.parse("3+4"))
//    println(parser.numericExpressionOnly.parse("3"))
    println(parser.numericExpressionOnly.parse("3+4"))
//    println(parser.test.parse("a"))
  }

}
