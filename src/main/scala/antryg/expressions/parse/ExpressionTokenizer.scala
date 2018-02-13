package antryg.expressions.parse

import java.util.regex.Pattern

import antryg.expressions.BinaryOperator
import antryg.expressions.parse.ExpressionTokenizer.{Token, TokenType, TokenizeFailure, TokenizeIssue, TokenizeResult, TokenizeSuccess}
import antryg.expressions.parse.ExpressionTokenizer.TokenType.ScanResult

import scala.util.Try

case class ExpressionTokenizer(symbols: ExpressionSymbols) {

  def tokenize(string: String): TokenizeResult = {
    var remainder: String = string.trim
    var tokens: Seq[Token] = Seq.empty
    var possibleNextTypes: Set[TokenType] = TokenType.possibleStartTokens
    var issues: Seq[TokenizeIssue] = Seq.empty
    var keepGoing: Boolean = true
    while (keepGoing && remainder.nonEmpty) {
      val scanResults = possibleNextTypes.flatMap(_.scan(remainder, symbols))
      if (scanResults.nonEmpty) {
        val biggestTokenSize = scanResults.map(_.token.string.size).max
        val bestResult = scanResults.filter(_.token.string.size == biggestTokenSize).head
        val token = bestResult.token
        tokens :+= token
        remainder = bestResult.remainder.trim
        possibleNextTypes = token.tokenType.canBeSucceededBy
      } else {
        val pos = string.size - remainder.size
        issues :+= TokenizeIssue(pos, "Cannot identify next token.")
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

  case class TokenizeIssue(pos: Int, message: String)

  trait TokenizeStatus

  object TokenizeSuccess extends TokenizeStatus

  case class TokenizeFailure(pos: Int, remainder: String) extends TokenizeStatus

  case class TokenizeResult(string: String, tokens: Seq[Token], issues: Seq[TokenizeIssue], status: TokenizeStatus) {
    def isSuccess: Boolean = status == TokenizeSuccess
  }

  trait TokenType {
    def scan(string: String, symbols: ExpressionSymbols): Option[ScanResult]

    def canBeSucceededBy: Set[TokenType]
  }

  object TokenType {

    case class ScanResult(token: Token, remainder: String)

    def possibleStartTokens: Set[TokenType] = Set(IdentifierType, LiteralType, OpenBracketType)

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

  trait OperandType extends TokenType {
    override def canBeSucceededBy: Set[TokenType] = Set(BinaryOperatorType, CloseBracketType)
  }

  object IdentifierType extends OperandType {
    override def scan(string: String, symbols: ExpressionSymbols): Option[ScanResult] = {
      if (Character.isJavaIdentifierStart(string.charAt(0))) {
        var size: Int = 1
        while (size < string.size && Character.isJavaIdentifierStart(string.charAt(size))) {
          size += 1
        }
        Some(ScanResult(Identifier(string.substring(0, size)), string.substring(size)))
      } else {
        None
      }
    }
  }

  object LiteralType extends OperandType {
    val startsWithNumberPattern: Pattern = Pattern.compile("^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?")

    override def scan(string: String, symbols: ExpressionSymbols): Option[ScanResult] = {
      val matcher = startsWithNumberPattern.matcher(string)
      if (matcher.find()) {
        val numberString = matcher.group()
        val remainder = string.substring(numberString.size)
        Try {
          numberString.toDouble
        }.fold(_ => None, value => Some(ScanResult(Literal(numberString, value), remainder)))
      } else {
        None
      }
    }
  }

  object BinaryOperatorType extends TokenType {
    override def scan(string: String, symbols: ExpressionSymbols): Option[ScanResult] = {
      val biOpSymbols = symbols.binaryOperators.keys.filter(string.startsWith(_))
      TokenType.chopOffLongestAtStart(string, biOpSymbols).map {
        case (biOpSymbol, remainder) =>
          val token = BinaryOperatorToken(biOpSymbol, symbols.binaryOperators(biOpSymbol))
          ScanResult(token, remainder)
      }
    }

    override def canBeSucceededBy: Set[TokenType] = Set(IdentifierType, LiteralType, OpenBracketType)
  }

  object OpenBracketType extends TokenType {
    override def scan(string: String, symbols: ExpressionSymbols): Option[ScanResult] = {
      TokenType.chopOffLongestAtStart(string, symbols.openBrackets).map {
        case (openBracketSymbol, remainder) =>
          val token = OpenBracket(openBracketSymbol)
          ScanResult(token, remainder)
      }
    }

    override def canBeSucceededBy: Set[TokenType] = TokenType.possibleStartTokens
  }

  object CloseBracketType extends TokenType {
    override def scan(string: String, symbols: ExpressionSymbols): Option[ScanResult] = {
      TokenType.chopOffLongestAtStart(string, symbols.closeBrackets).map {
        case (closeBracketSymbol, remainder) =>
          val token = CloseBracket(closeBracketSymbol)
          ScanResult(token, remainder)
      }
    }

    override def canBeSucceededBy: Set[TokenType] = Set(BinaryOperatorType, CloseBracketType)
  }

  trait Token {
    def tokenType: TokenType

    def string: String

    def size: Int = string.length
  }

  case class Identifier(string: String) extends Token {
    override def tokenType: IdentifierType.type = IdentifierType
  }

  case class Literal(string: String, value: Double) extends Token {
    override def tokenType: LiteralType.type = LiteralType
  }

  case class BinaryOperatorToken(string: String, binaryOperator: BinaryOperator.Base) extends Token {
    override def tokenType: BinaryOperatorType.type = BinaryOperatorType
  }

  case class OpenBracket(string: String) extends Token {
    override def tokenType: OpenBracketType.type = OpenBracketType
  }

  case class CloseBracket(string: String) extends Token {
    override def tokenType: CloseBracketType.type = CloseBracketType
  }

}
