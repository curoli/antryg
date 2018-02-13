package antryg.expressions.parse

import antryg.expressions.Expression
import antryg.expressions.parse.ExpressionParser.Issue.Stage

case class ExpressionParser(symbols: ExpressionSymbols) {


  def parse[T](string: String): Either[Seq[String], Expression[T]] = {
    var levels: Seq[Int] = Seq.empty
    var currentLevel: Int = 0
    for (char <- string) {
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

object ExpressionParser {

  case class Issue(message: String, pos: Int, stage: Stage, isFatal: Boolean)

  object Issue {

    trait Stage

    case object Tokenization extends Stage

    case object Analysis extends Stage

  }

}
