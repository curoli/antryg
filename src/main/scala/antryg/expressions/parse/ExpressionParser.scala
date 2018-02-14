package antryg.expressions.parse

import antryg.expressions.Expression
import antryg.expressions.parse.ExpressionParser.Issue.Stage
import antryg.expressions.parse.ExpressionParser.{Issue, ParseFailedAnalysis, ParseFailedTokenizing, ParseGotLogicalExpression, ParseGotNumericalExpression, ParseResult}
import antryg.expressions.parse.ExpressionTokenAnalyzer.{AnalysisFailure, AnalysisGotLogicalExpression, AnalysisGotNumericalExpression, TokenCursor}

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
