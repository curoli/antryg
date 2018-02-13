package antryg.expressions.parse

import java.util.regex.Pattern

import antryg.expressions.numeric.{NumericConstant, NumericVariable}
import antryg.expressions.{BinaryOperator, Expression}
import antryg.expressions.parse.ExpressionParser.Issue
import antryg.expressions.parse.ExpressionTokenizer.TokenType.ScanResult
import antryg.expressions.parse.ExpressionTokenizer.{Token, TokenType, TokenizeFailure, TokenizeResult, TokenizeSuccess}

import scala.util.Try

case class ExpressionTokenizer(symbols: ExpressionSymbols) {

  def tokenize(string: String): TokenizeResult = {
    var remainder: String = string.trim
    var tokens: Seq[Token] = Seq.empty
    var possibleNextTypes: Set[TokenType] = TokenType.possibleStartTokens
    var issues: Seq[Issue] = Seq.empty
    var keepGoing: Boolean = true
    while (keepGoing && remainder.nonEmpty) {
      val pos = string.size - remainder.size
      val scanResults = possibleNextTypes.flatMap(_.scan(remainder, pos, symbols))
      if (scanResults.nonEmpty) {
        val biggestTokenSize = scanResults.map(_.token.string.size).max
        val bestResult = scanResults.filter(_.token.string.size == biggestTokenSize).head
        val token = bestResult.token
        tokens :+= token
        remainder = bestResult.remainder.trim
        possibleNextTypes = token.tokenType.canBeSucceededBy
      } else {
        issues :+= Issue("Cannot identify next token.", pos, Issue.Tokenization, isFatal = true)
        keepGoing = false
      }
    }
    val status =
      if (remainder.isEmpty) {
        TokenizeSuccess
      } else {
        val pos = string.size - remainder.size
        TokenizeFailure(pos, remainder)
      }
    TokenizeResult(string, tokens, issues, status)
  }

}

object ExpressionTokenizer {

  trait TokenizeStatus

  object TokenizeSuccess extends TokenizeStatus

  case class TokenizeFailure(pos: Int, remainder: String) extends TokenizeStatus

  case class TokenizeResult(string: String, tokens: Seq[Token], issues: Seq[Issue], status: TokenizeStatus) {
    def isSuccess: Boolean = status == TokenizeSuccess
  }

  trait TokenType {
    def scan(string: String, pos: Int, symbols: ExpressionSymbols): Option[ScanResult]

    def canBeSucceededBy: Set[TokenType]
  }

  object TokenType {

    case class ScanResult(token: Token, remainder: String)

    def possibleStartTokens: Set[TokenType] = Set(ExpressionType, OpenBracketType)

    def chopOffLongestAtStart(string: String, symbols: Iterable[String]): Option[(String, String)] = {
      val matchingSymbols = symbols.filter(string.startsWith(_))
      if (matchingSymbols.nonEmpty) {
        val sizeMax = matchingSymbols.map(_.size).max
        val matchingSymbol = matchingSymbols.filter(_.size == sizeMax).head
        val remainder = string.substring(sizeMax)
        Some((matchingSymbol, remainder))
      } else {
        None
      }
    }
  }

  object ExpressionType extends TokenType {
    val startsWithNumberPattern: Pattern = Pattern.compile("^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?")

    override def scan(string: String, pos: Int, symbols: ExpressionSymbols): Option[ScanResult] = {
      val matcher = startsWithNumberPattern.matcher(string)
      if (matcher.find()) {
        val numberString = matcher.group()
        val remainder = string.substring(numberString.size)
        Try {
          numberString.toDouble
        }.fold(
          _ => None,
          value => Some(ScanResult(ExpressionToken(numberString, pos, NumericConstant(value)), remainder))
        )
      } else {
        if (Character.isJavaIdentifierStart(string.charAt(0))) {
          var size: Int = 1
          while (size < string.size && Character.isJavaIdentifierPart(string.charAt(size))) {
            size += 1
          }
          val identifier = string.substring(0, size)
          Some(ScanResult(ExpressionToken(identifier, pos, NumericVariable(identifier)), string.substring(size)))
        } else {
          None
        }
      }
    }

    override def canBeSucceededBy: Set[TokenType] = Set(BinaryOperatorType, CloseBracketType)
  }

  object BinaryOperatorType extends TokenType {
    override def scan(string: String, pos: Int, symbols: ExpressionSymbols): Option[ScanResult] = {
      val biOpSymbols = symbols.binaryOperators.keys.filter(string.startsWith(_))
      TokenType.chopOffLongestAtStart(string, biOpSymbols).map {
        case (biOpSymbol, remainder) =>
          val token = BinaryOperatorToken(biOpSymbol, pos, symbols.binaryOperators(biOpSymbol))
          ScanResult(token, remainder)
      }
    }

    override def canBeSucceededBy: Set[TokenType] = Set(ExpressionType, OpenBracketType)
  }

  object OpenBracketType extends TokenType {
    override def scan(string: String, pos: Int, symbols: ExpressionSymbols): Option[ScanResult] = {
      TokenType.chopOffLongestAtStart(string, symbols.openBrackets).map {
        case (openBracketSymbol, remainder) =>
          val token = OpenBracket(openBracketSymbol, pos)
          ScanResult(token, remainder)
      }
    }

    override def canBeSucceededBy: Set[TokenType] = TokenType.possibleStartTokens
  }

  object CloseBracketType extends TokenType {
    override def scan(string: String, pos: Int, symbols: ExpressionSymbols): Option[ScanResult] = {
      TokenType.chopOffLongestAtStart(string, symbols.closeBrackets).map {
        case (closeBracketSymbol, remainder) =>
          val token = CloseBracket(closeBracketSymbol, pos)
          ScanResult(token, remainder)
      }
    }

    override def canBeSucceededBy: Set[TokenType] = Set(BinaryOperatorType, CloseBracketType)
  }

  trait Token {
    def tokenType: TokenType

    def string: String

    def pos: Int

    def size: Int = string.length
  }

  case class ExpressionToken(string: String, pos: Int, expression: Expression.Base) extends Token {
    override def tokenType: ExpressionType.type = ExpressionType
  }

  case class BinaryOperatorToken(string: String, pos: Int, binaryOperator: BinaryOperator.Base) extends Token {
    override def tokenType: BinaryOperatorType.type = BinaryOperatorType
  }

  case class OpenBracket(string: String, pos: Int) extends Token {
    override def tokenType: OpenBracketType.type = OpenBracketType
  }

  case class CloseBracket(string: String, pos: Int) extends Token {
    override def tokenType: CloseBracketType.type = CloseBracketType
  }

}
