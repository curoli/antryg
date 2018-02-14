package antryg.expressions.parse

import antryg.expressions.{BinaryExpression, BinaryOperator, Expression}
import antryg.expressions.parse.ExpressionParser.Issue
import antryg.expressions.parse.ExpressionTokenAnalyzer.TokenCursor.{Failure, GotLogicalExpression, GotNumericExpression, MoreToDo}
import antryg.expressions.parse.ExpressionTokenizer.{BinaryOperatorToken, CloseBracket, ExpressionToken, OpenBracket, Token}

object ExpressionTokenAnalyzer {

  trait AnalysisResult

  case class TokenCursor(tokensBefore: Seq[Token], currentToken: Token, tokensAfter: Seq[Token]) {
    def pos: Int = tokensBefore.size

    def size: Int = tokensBefore.size + 1 + tokensAfter.size

    def isAtFirst: Boolean = tokensBefore.isEmpty

    def isAtLast: Boolean = tokensAfter.isEmpty

    def tokenLeftOpt: Option[Token] = tokensBefore.lastOption

    def tokenLeftLeftOpt: Option[Token] = tokensBefore.dropRight(1).lastOption

    def tokenRightOpt: Option[Token] = tokensAfter.headOption

    def tokenRightRightOpt: Option[Token] = tokensAfter.drop(1).headOption

    def stepLeft: TokenCursor =
      if (tokensBefore.nonEmpty) {
        TokenCursor(
          tokensBefore = tokensBefore.dropRight(1),
          currentToken = tokensBefore.last,
          tokensAfter = currentToken +: tokensAfter
        )
      } else {
        this
      }

    def stepRight: TokenCursor =
      if (tokensAfter.nonEmpty) {
        TokenCursor(
          tokensBefore = tokensBefore :+ currentToken,
          currentToken = tokensAfter.head,
          tokensAfter = tokensAfter.tail
        )
      } else {
        this
      }

    def isOnBinaryOperator: Boolean = currentToken.isInstanceOf[BinaryOperatorToken]

    def step: TokenCursor.Status = {
      (tokenLeftLeftOpt, tokenLeftOpt, currentToken, tokenRightOpt, tokenRightRightOpt) match {
        case (None, None, ExpressionToken(_, _, expression), None, None) =>
          expression.theType match {
            case Expression.Numeric => GotNumericExpression(expression.asInstanceOf[Expression[Double]])
            case Expression.Logical => GotLogicalExpression(expression.asInstanceOf[Expression[Boolean]])
          }
        case (None, None, token: Token, None, None) =>
          val issue = Issue("Did not end up with an expression", token.pos, Issue.Analysis, isFatal = true)
          Failure(this, Seq(issue))
        case (_, Some(OpenBracket(_, _)), ExpressionToken(_, _, _), Some(CloseBracket(_, _)), _) =>
          MoreToDo(copy(tokensBefore = tokensBefore.dropRight(1), tokensAfter = tokensAfter.tail))
        case
          (_, Some(BinaryOperatorToken(_, _, opLeft)), ExpressionToken(_, _, _),
          Some(BinaryOperatorToken(_, _, opRight)), _) =>
          if(opLeft.precedence < opRight.precedence) MoreToDo(stepRight) else MoreToDo(stepLeft)
        case
          (Some(BinaryOperatorToken(_, _, opLeft)), Some(ExpressionToken(_, posLeft, expLeft)),
          BinaryOperatorToken(_, _, op), Some(ExpressionToken(_, _, expRight)),
          Some(BinaryOperatorToken(_, _, opRight))) =>
          if(opLeft.precedence >= op.precedence) {
            MoreToDo(stepLeft.stepLeft)
          } else {
            if(op.precedence < opRight.precedence) {
              MoreToDo(stepRight.stepRight)
            } else {
              BinaryExpression.create(expLeft, op, expRight) match {
                case Left(message) => Failure(this, Seq(Issue(message, posLeft, Issue.Analysis, isFatal = true)))
                case Right(expression) =>
                  MoreToDo(TokenCursor(
                    tokensBefore = tokensBefore.dropRight(1),
                    currentToken = ExpressionToken(expression.asString, posLeft, expression),
                    tokensAfter = tokensAfter.tail
                  ))
              }
            }
          }
      }
    }

  }

  object TokenCursor{
    trait Status
    case class Failure(tokenCursor: TokenCursor, issues: Seq[Issue]) extends Status
    case class GotNumericExpression(expression: Expression[Double]) extends Status
    case class GotLogicalExpression(expression: Expression[Boolean]) extends Status
    case class MoreToDo(tokenCursor: TokenCursor) extends Status
  }

  def analyze(tokens: Seq[Token]): AnalysisResult = {
    ???
  }

}
