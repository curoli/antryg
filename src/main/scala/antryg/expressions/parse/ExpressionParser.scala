package antryg.expressions.parse

import antryg.expressions.Expression

object ExpressionParser {

  def parse[T](string: String): Either[Seq[String], Expression[T]] = {
    Left(Seq("Not yet implemented"))
  }

}
