package antryg.expressions.logical

import antryg.expressions.numeric.NumericVariable
import antryg.expressions.{Constant, Expression, Variable}

case class BooleanConstant(value: Boolean) extends Constant[Boolean] {
  override def theType: Expression.Type = Expression.Logical

  override def valueOpt: Some[Boolean] = Some(value)

  override def variables: Set[Variable[_]] = Set.empty

  override def numericVariables: Set[NumericVariable] = Set.empty

  override def booleanVariables: Set[BooleanVariable] = Set.empty

  override def bindNumber(varName: String, value: Double): BooleanConstant = this

  override def bindBoolean(varName: String, value: Boolean): BooleanConstant = this

  override def bind(numberValues: Map[String, Double], booleanValues: Map[String, Boolean]): BooleanConstant = this
}
