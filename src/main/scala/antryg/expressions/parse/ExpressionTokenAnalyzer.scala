package antryg.expressions.parse

import antryg.expressions.logical.BooleanExpression
import antryg.expressions.numeric.NumericExpression
import antryg.expressions.{BinaryExpression, BinaryOperator, Expression}
import antryg.expressions.parse.ExpressionParser.Issue
import antryg.expressions.parse.ExpressionTokenAnalyzer.TokenCursor.{Failure, GotLogicalExpression, GotNumericExpression, MoreToDo}
import antryg.expressions.parse.ExpressionTokenizer.{BinaryOperatorToken, CloseBracket, ExpressionToken, OpenBracket, Token}

object ExpressionTokenAnalyzer {

  sealed trait AnalysisResult

  case class AnalysisFailure(tokenCursor: TokenCursor, issues: Seq[Issue]) extends AnalysisResult

  case class AnalysisGotLogicalExpression(expression: BooleanExpression) extends AnalysisResult

  case class AnalysisGotNumericalExpression(expression: NumericExpression) extends AnalysisResult

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

    def createBinaryExpression(lhs: Expression.Base, op: BinaryOperator.Base,
                               rhs: Expression.Base, posLeft: Int): TokenCursor.Status = {
      BinaryExpression.create(lhs, op, rhs) match {
        case Left(message) => Failure(this, Seq(Issue(message, posLeft, Issue.Analysis, isFatal = true)))
        case Right(expression) =>
          MoreToDo(TokenCursor(
            tokensBefore = tokensBefore.dropRight(1),
            currentToken = ExpressionToken(expression.asString, posLeft, expression),
            tokensAfter = tokensAfter.tail
          ))
      }
    }

    def step: TokenCursor.Status = {
      (tokenLeftLeftOpt, tokenLeftOpt, currentToken, tokenRightOpt, tokenRightRightOpt) match {
        case (None, None, ExpressionToken(_, _, expression), None, None) =>
          expression.theType match {
            case Expression.Numeric => GotNumericExpression(expression.asInstanceOf[NumericExpression])
            case Expression.Logical => GotLogicalExpression(expression.asInstanceOf[BooleanExpression])
          }
        case (None, None, token: Token, None, None) =>
          val issue = Issue(Issue.Messages.notAnExpressionAtEnd, token.pos, Issue.Analysis, isFatal = true)
          Failure(this, Seq(issue))
        case (_, _, CloseBracket(_, _), _, _) => MoreToDo(stepLeft)
        case (_, _, OpenBracket(_, _), _, _) => MoreToDo(stepRight)
        case (_, Some(OpenBracket(_, _)), ExpressionToken(_, _, _), Some(CloseBracket(_, _)), _) =>
          MoreToDo(copy(tokensBefore = tokensBefore.dropRight(1), tokensAfter = tokensAfter.tail))
        case
          (_, Some(BinaryOperatorToken(_, _, opLeft)), ExpressionToken(_, _, _),
          Some(BinaryOperatorToken(_, _, opRight)), _) =>
          if (opLeft.precedence < opRight.precedence) MoreToDo(stepRight) else MoreToDo(stepLeft)
        case (_, Some(BinaryOperatorToken(_, _, _)), ExpressionToken(_, _, _), _, _) => MoreToDo(stepLeft)
        case (_, _, ExpressionToken(_, _, _), Some(BinaryOperatorToken(_, _, _)), _) => MoreToDo(stepRight)
        case (_, Some(CloseBracket(_, _)), BinaryOperatorToken(_, _, _), _, _) => MoreToDo(stepLeft)
        case (_, _, BinaryOperatorToken(_, _, _), Some(OpenBracket(_, _)), _) => MoreToDo(stepRight)
        case
          (Some(BinaryOperatorToken(_, _, opLeft)), Some(ExpressionToken(_, posLeft, expLeft)),
          BinaryOperatorToken(_, _, op), Some(ExpressionToken(_, _, expRight)),
          Some(BinaryOperatorToken(_, _, opRight))) =>
          if (opLeft.precedence >= op.precedence) {
            MoreToDo(stepLeft.stepLeft)
          } else {
            if (op.precedence < opRight.precedence) {
              MoreToDo(stepRight.stepRight)
            } else {
              createBinaryExpression(expLeft, op, expRight, posLeft)
            }
          }
        case
          (Some(BinaryOperatorToken(_, _, opLeft)), Some(ExpressionToken(_, posLeft, expLeft)),
          BinaryOperatorToken(_, _, op), Some(ExpressionToken(_, _, expRight)), _) =>
          if (opLeft.precedence >= op.precedence) {
            MoreToDo(stepLeft.stepLeft)
          } else {
            createBinaryExpression(expLeft, op, expRight, posLeft)
          }
        case
          (_, Some(ExpressionToken(_, posLeft, expLeft)), BinaryOperatorToken(_, _, op),
          Some(ExpressionToken(_, _, expRight)), Some(BinaryOperatorToken(_, _, opRight))) =>
          if (op.precedence < opRight.precedence) {
            MoreToDo(stepRight.stepRight)
          } else {
            createBinaryExpression(expLeft, op, expRight, posLeft)
          }
        case
          (_, Some(ExpressionToken(_, posLeft, expLeft)), BinaryOperatorToken(_, _, op),
          Some(ExpressionToken(_, _, expRight)), _) =>
          createBinaryExpression(expLeft, op, expRight, posLeft)
        case (_, _, _, _, _) =>
          val rogueTokens =
            Seq(tokenLeftLeftOpt, tokenLeftOpt, Some(currentToken), tokenRightOpt, tokenRightRightOpt).flatten
          val message = Issue.Messages.cannotAnalyzeTokenSequence(rogueTokens)
          Failure(this, Seq(Issue(message, currentToken.pos, Issue.Analysis, isFatal = true)))
      }
    }

  }

  object TokenCursor {

    def apply(tokens: Seq[Token]): TokenCursor = TokenCursor(Seq.empty, tokens.head, tokens.tail)

    trait Status

    case class Failure(tokenCursor: TokenCursor, issues: Seq[Issue]) extends Status

    case class GotNumericExpression(expression: NumericExpression) extends Status

    case class GotLogicalExpression(expression: BooleanExpression) extends Status

    case class MoreToDo(tokenCursor: TokenCursor) extends Status

  }

  def analyze(tokens: Seq[Token]): AnalysisResult = {
    var cursor: TokenCursor = TokenCursor(tokens)
    var resultOpt: Option[AnalysisResult] = None
    while (resultOpt.isEmpty) {
      cursor.step match {
        case Failure(tokenCursor, issues) => resultOpt = Some(AnalysisFailure(tokenCursor, issues))
        case GotLogicalExpression(expression) => resultOpt = Some(AnalysisGotLogicalExpression(expression))
        case GotNumericExpression(expression) => resultOpt = Some(AnalysisGotNumericalExpression(expression))
        case MoreToDo(cursorNew) => cursor = cursorNew
      }
    }
    resultOpt.get
  }

}
