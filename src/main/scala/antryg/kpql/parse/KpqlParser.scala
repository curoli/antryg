package antryg.kpql.parse

import antryg.expressions.parse.ExpressionParser
import antryg.kpql.KpqlQuery

object KpqlParser {

def parse(string: String): Either[String, KpqlQuery] = {
  val splitByQMark = string.split("\\?")
  if(splitByQMark.size != 2) {
    Left("Query needs to contain exactly one '?'.")
  } else {
    val preQMarkSplitByAt = splitByQMark(0).split("@")
    if(preQMarkSplitByAt.size != 2) {
      Left("Query needs to contain exactly one '@'.")
    } else {
      val phenotype = preQMarkSplitByAt(0).trim
      val datasets = preQMarkSplitByAt(1).split(",").map(_.trim).filter(_.nonEmpty).toSeq
      val expressionString = splitByQMark(1)
      ExpressionParser.default.parse(expressionString) match {
        case failure: ExpressionParser.ParseFailure =>
          Left(s"Could not parse filter expression: ${failure.issues.map(_.message).mkString(", ")}")
        case _ : ExpressionParser.ParseGotNumericalExpression =>
          Left("Filter expression needs to result in boolean, not number.")
        case ExpressionParser.ParseGotLogicalExpression(expression) =>
          Right(KpqlQuery(phenotype, datasets, expression))
      }
    }
  }
}

}
