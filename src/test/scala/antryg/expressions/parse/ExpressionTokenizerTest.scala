package antryg.expressions.parse

import org.scalatest.FunSuite

class ExpressionTokenizerTest extends FunSuite {

  test("tokenize") {
    val symbols = ExpressionSymbols.default
    val tokenizer = ExpressionTokenizer(symbols)
    val result = tokenizer.tokenize("2+3+x+y")
    println(result)
  }

}
