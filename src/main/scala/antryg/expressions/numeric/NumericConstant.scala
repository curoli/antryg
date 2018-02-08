package antryg.expressions.numeric

import antryg.expressions.logical.BooleanVariable
import antryg.expressions.{Constant, Variable}

case class NumericConstant(value: Double) extends NumericExpression with Constant {
  override def variables: Set[Variable] = Set.empty

  override def numericVariables: Set[NumericVariable] = Set.empty

  override def booleanVariables: Set[BooleanVariable] = Set.empty

  override def bindNumber(varName: String, value: Double): NumericConstant = this

  override def bindBoolean(varName: String, value: Boolean): NumericConstant = this

  override def bind(numberValues: Map[String, Double], booleanValues: Map[String, Boolean]): NumericConstant = this
}
