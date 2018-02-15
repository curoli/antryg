package antryg.expressions.parse

import antryg.expressions.{BinaryOperator, Expression}
import antryg.expressions.parse.ExpressionParser.Issue.Stage
import antryg.expressions.parse.ExpressionParser.{ParseFailedAnalysis, ParseFailedTokenizing, ParseGotLogicalExpression, ParseGotNumericalExpression, ParseResult}
import antryg.expressions.parse.ExpressionTokenAnalyzer.{AnalysisFailure, AnalysisGotLogicalExpression, AnalysisGotNumericalExpression, TokenCursor}
import antryg.expressions.parse.ExpressionTokenizer.Token

case class ExpressionParser(symbols: ExpressionSymbols) {

  val tokenizer: ExpressionTokenizer = ExpressionTokenizer(symbols)
  val analyzer: ExpressionTokenAnalyzer.type = ExpressionTokenAnalyzer

  def parse(string: String): ParseResult = {
    val tokenizeResult = tokenizer.tokenize(string)
    if (!tokenizeResult.isSuccess) {
      ParseFailedTokenizing(string, tokenizeResult.pos, tokenizeResult.issues)
    } else {
      analyzer.analyze(tokenizeResult.tokens) match {
        case AnalysisFailure(tokenCursor, issues) => ParseFailedAnalysis(issues, tokenCursor)
        case AnalysisGotLogicalExpression(expression) => ParseGotLogicalExpression(expression)
        case AnalysisGotNumericalExpression(expression) => ParseGotNumericalExpression(expression)
      }
    }
  }

}

object ExpressionParser {

  case class Issue(message: String, pos: Int, stage: Stage, isFatal: Boolean)

  object Issue {

    trait Stage

    case object Tokenization extends Stage

    case object Analysis extends Stage

    object Messages {
      val notAnExpressionAtEnd = "Did not end up with an expression"
      val closingBracketWithoutOpening = "Closing bracket with no matching opening bracket"
      val openingBracketWithoutClosing = "Opening bracket with no matching closing bracket"
      val stringIsEmpty = "String is empty"
      val cannotIdentifyNextToken = "Cannot identify next token"

      def lhsWrongType(lhsType: Expression.Type, op: BinaryOperator.Base): String =
        s"Left expression has type $lhsType, but operator ${op.symbol} needs ${op.lhsType}"

      def rhsWrongType(rhsType: Expression.Type, op: BinaryOperator.Base): String =
        s"Right expression has type $rhsType, but operator ${op.symbol} needs ${op.rhsType}"

      def cannotAnalyzeTokenSequence(tokens: Seq[Token]): String =
        s"Don't know what to do with token sequence (${tokens.mkString(", ")})."
    }

  }

  sealed trait ParseResult

  sealed trait ParseFailure extends ParseResult {
    def issues: Seq[Issue]
  }

  case class ParseFailedTokenizing(string: String, pos: Int, issues: Seq[Issue]) extends ParseFailure

  case class ParseFailedAnalysis(issues: Seq[Issue], tokenCursor: TokenCursor) extends ParseFailure

  sealed trait ParseSuccess extends ParseResult {
    def expression: Expression.Base
  }

  case class ParseGotLogicalExpression(expression: Expression[Boolean]) extends ParseSuccess

  case class ParseGotNumericalExpression(expression: Expression[Double]) extends ParseSuccess

}
