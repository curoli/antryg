package antryg.expressions.parse

import antryg.expressions.BinaryExpression.ArithmeticBinaryExpression
import antryg.expressions.BinaryOperator.ArithmeticOperator
import antryg.expressions.logical.{BooleanConstant, BooleanVariable}
import antryg.expressions.numeric.{NumericConstant, NumericExpression, NumericVariable}
import fastparse.all.{CharIn, CharPred, CharsWhileIn, P, parserApi, Parser, _}

object ExpressionFastParser {

  val space: Parser[Unit] = P(CharPred(_.isWhitespace).rep)

  val digits: Parser[Unit] = P(CharsWhileIn("0123456789"))
  val exponent: Parser[Unit] = P(CharIn("eE") ~ CharIn("+-").? ~ digits)
  val fractional: Parser[Unit] = P("." ~ digits)
  val integral: Parser[Unit] = P("0" | CharIn('1' to '9') ~ digits.?)

  val numericConstant: Parser[NumericConstant] =
    P(CharIn("+-").? ~ integral ~ fractional.? ~ exponent.?).!.map { numberString =>
      NumericConstant(numberString.toDouble)
    }

  val constantTrue: Parser[BooleanConstant] = P("true").map(_ => BooleanConstant(true))
  val constantFalse: Parser[BooleanConstant] = P("false").map(_ => BooleanConstant(false))

  val identifier: Parser[Unit] = P(CharPred(_.isUnicodeIdentifierStart) ~ CharPred(_.isUnicodeIdentifierPart).rep)

  val numericVariable: Parser[NumericVariable] =
    P(identifier).!.map(identifier => NumericVariable(identifier))
  val booleanVariable: Parser[BooleanVariable] =
    P(identifier).!.map(identifier => BooleanVariable(identifier))

  val numericExpression: Parser[NumericExpression] = P(numericIndivisible | plusMinus)

  val numericInParens: Parser[NumericExpression] =
    P(P("(") ~ space ~ numericExpression ~ space ~ P(")"))

  val numericAtomic: Parser[NumericExpression] = P(numericConstant | numericVariable)

  val numericIndivisible: Parser[NumericExpression] = P(numericAtomic | numericInParens)

  val plusMinusLeft: Parser[NumericExpression] = P(numericIndivisible | plusMinus)

  val plusMinusRight: Parser[NumericExpression] = P(numericIndivisible)

  val plusOp: Parser[ArithmeticOperator] = P("+").!.map(_ => ArithmeticOperator.plus)
  val minusOp: Parser[ArithmeticOperator] = P("-").!.map(_ => ArithmeticOperator.plus)
  val plusMinus: Parser[ArithmeticBinaryExpression] =
    P(plusMinusLeft ~ space ~ (plusOp | minusOp) ~ space ~ plusMinusRight).map {
      case (lhs, op, rhs) => ArithmeticBinaryExpression(lhs, op, rhs)
    }

  val timesDividedByLeft: Parser[NumericExpression] = P(numericIndivisible)
  val timesDividedByRight: Parser[NumericExpression] = P(numericIndivisible)
  val timesOp: Parser[ArithmeticOperator] = P("*").!.map(_ => ArithmeticOperator.times)
  val dividedByOp: Parser[ArithmeticOperator] = P("/").!.map(_ => ArithmeticOperator.dividedBy)

  val numericExpressionOnly: Parser[NumericExpression] = P(Start ~ space ~ numericExpression ~ space ~ End)

  val test: Parser[String] = P(Start ~ space ~ "a".! ~ space ~ End)

}

