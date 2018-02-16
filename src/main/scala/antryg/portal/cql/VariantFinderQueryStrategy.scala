package antryg.portal.cql

import antryg.cql.builder.Select.{BinaryClause, Equals}
import antryg.expressions.BinaryExpression.{BooleanBinaryExpression, NumericalComparisonExpression}
import antryg.expressions.BinaryOperator.{BooleanOperator, NumericalComparisonOperator}
import antryg.expressions.logical.BooleanExpression
import antryg.expressions.transform.ConstantPropagator

object VariantFinderQueryStrategy {

  def extractClauses(filter: BooleanExpression): Set[BinaryClause] = {
    filter match {
      case BooleanBinaryExpression(rhs, BooleanOperator.and, lhs) =>
        extractClauses(lhs) ++ extractClauses(rhs)
      case compExpr: NumericalComparisonExpression => BinaryClause.fromComparisonExpression(compExpr).toSet
      case _ => Set.empty
    }
  }

  def valueName(clauses: Set[BinaryClause]): Either[String, String] = {
    if (clauses.isEmpty) {
      Left("No suitable clauses available.")
    } else {
      val valueNameCounts = clauses.groupBy(_.column).mapValues(_.size)
      val maxCount = valueNameCounts.values.max
      Right(clauses.filter(clause => valueNameCounts(clause.column) == maxCount).head.column)
    }
  }

  def getClausesForVariantIndex(valueName: String, clauses: Set[BinaryClause]): Set[BinaryClause] =
    clauses.filter(_.column == valueName).map(_.withColumn(VariantFinderSchema.Cols.value.name))

  def createPlan(filter: BooleanExpression): Either[String, VariantFinderQueryPlan] = {
    val filterSimplified = ConstantPropagator.transformBoolean(filter)
    val potentialClauses = extractClauses(filterSimplified)
    valueName(potentialClauses) match {
      case left: Left[String, VariantFinderQueryPlan] => left
      case Right(valueName) =>
        val clauses = getClausesForVariantIndex(valueName, potentialClauses)
        Right(VariantFinderQueryPlan(valueName, clauses, filter))
    }
  }

}
