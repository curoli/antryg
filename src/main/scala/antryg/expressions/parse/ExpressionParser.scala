package antryg.expressions.parse

import antryg.expressions.{BinaryOperator, Expression}

case class ExpressionParser(binaryOps: Iterable[BinaryOperator.Base]) {



  def parse[T](string: String): Either[Seq[String], Expression[T]] = {
    var levels: Seq[Int] = Seq.empty
    var currentLevel: Int = 0
    for(char <- string) {
      currentLevel += (char match {
        case '(' => 1
        case ')' => -1
        case _ => 0
      })
      levels :+= currentLevel
    }
    Left(Seq("Not yet implemented"))
  }

}
