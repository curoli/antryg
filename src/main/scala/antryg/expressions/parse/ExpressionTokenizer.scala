package antryg.expressions.parse

import antryg.expressions.BinaryOperator
import antryg.expressions.parse.ExpressionTokenizer.Token

case class ExpressionTokenizer(symbols: ExpressionSymbols) {

  def tokenize(string: String): Seq[Token] = {
    ???
  }

}

object ExpressionTokenizer {

  trait TokenType {
    def scan(string: String): Option[Token]

    def canBeSucceededBy: Set[TokenType]
  }

  object TokenType {
    def firstTokenCanBe: Set[TokenType] = Set(IdentifierType, LiteralType, OpenBracketType)
  }

  trait OperandType extends TokenType {
    override def canBeSucceededBy: Set[TokenType] = Set(BinaryOperatorType, CloseBracketType)
  }

  object IdentifierType extends OperandType {
    override def scan(string: String): Option[Token] = ???
  }

  object LiteralType extends OperandType {
    override def scan(string: String): Option[Token] = ???
  }

  object BinaryOperatorType extends TokenType {
    override def scan(string: String): Option[Token] = ???

    override def canBeSucceededBy: Set[TokenType] = Set(IdentifierType, LiteralType, OpenBracketType)
  }

  object OpenBracketType extends TokenType {
    override def scan(string: String): Option[Token] = ???

    override def canBeSucceededBy: Set[TokenType] = TokenType.firstTokenCanBe
  }

  object CloseBracketType extends TokenType {
    override def scan(string: String): Option[Token] = ???

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
