package antryg.expressions.parse

import antryg.expressions.parse.ExpressionTokenizer.{TokenizeFailure, TokenizeResult, TokenizeSuccess}
import org.scalatest.FunSuite

class ExpressionTokenizerTest extends FunSuite {

  def assertTokenizingSucceeds(tokenizer: ExpressionTokenizer, string: String): Unit = {
    val result = tokenizer.tokenize(string)
    val messageIfFailure = result.toString
    assert(result.tokens.nonEmpty, messageIfFailure)
    assert(result.issues.isEmpty, messageIfFailure)
    assert(result.status === TokenizeSuccess, messageIfFailure)
    assert(result.isSuccess, messageIfFailure)
  }

  def assertTokenizingFails(tokenizer: ExpressionTokenizer, string: String): Unit = {
    val result = tokenizer.tokenize(string)
    val messageIfFailure = result.toString
    assert(result.issues.nonEmpty, messageIfFailure)
    assert(result.status.isInstanceOf[TokenizeFailure], messageIfFailure)
    assert(!result.isSuccess, messageIfFailure)
  }

  test("tokenize") {
    val symbols = ExpressionSymbols.default
    val tokenizer = ExpressionTokenizer(symbols)
    val validStrings =
      Seq("1", "x", "x42", "1+y", "(1+x)*(1-y)", "9*x*x + 6*x + 1", "x+y = z", "a < b & b < c",
      "x1 <= x2 & x1 >= x3", "-1", "+1", "(2)")
    validStrings.foreach(assertTokenizingSucceeds(tokenizer, _))
    val invalidStrings =
      Seq("", "1 2", "x y", "@", "1++2","x(y)", "()")
  }

}
