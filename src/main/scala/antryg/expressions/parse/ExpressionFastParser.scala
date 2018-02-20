package antryg.expressions.parse

import antryg.expressions.logical.{BooleanConstant, BooleanVariable}
import antryg.expressions.numeric.{NumericConstant, NumericVariable}
import fastparse.all.{CharIn, CharPred, CharsWhileIn, P, parserApi, _}
import fastparse.{all, core}

object ExpressionFastParser {

  val space: all.Parser[Unit] = P(CharPred(_.isWhitespace))

  val digits: all.Parser[Unit] = P(CharsWhileIn("0123456789"))
  val exponent: all.Parser[Unit] = P(CharIn("eE") ~ CharIn("+-").? ~ digits)
  val fractional: all.Parser[Unit] = P("." ~ digits)
  val integral: all.Parser[Unit] = P("0" | CharIn('1' to '9') ~ digits.?)

  val numericConstant: core.Parser[NumericConstant, Char, String] =
    P(CharIn("+-").? ~ integral ~ fractional.? ~ exponent.?).!.map { numberString =>
      NumericConstant(numberString.toDouble)
    }

  val constantTrue: core.Parser[BooleanConstant, Char, String] = P("true").map(_ => BooleanConstant(true))
  val constantFalse: core.Parser[BooleanConstant, Char, String] = P("false").map(_ => BooleanConstant(false))

  val identifier: all.Parser[Unit] = P(CharPred(_.isUnicodeIdentifierStart) ~ CharPred(_.isUnicodeIdentifierPart).rep)

  val numericVariable: core.Parser[NumericVariable, Char, String] =
    P(identifier).!.map(identifier => NumericVariable(identifier))
  val booleanVariable: core.Parser[BooleanVariable, Char, String] =
    P(identifier).!.map(identifier => BooleanVariable(identifier))

}

