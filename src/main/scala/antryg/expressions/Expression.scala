package antryg.expressions

import antryg.expressions.logical.BooleanVariable
import antryg.expressions.numeric.NumericVariable

trait Expression {
  def variables: Set[Variable]

  def numericVariables: Set[NumericVariable]

  def booleanVariables: Set[BooleanVariable]

  def bindNumber(varName: String, value: Double): Expression

  def bindBoolean(varName: String, value: Boolean): Expression

  def bind(numberValues: Map[String, Double], booleanValues: Map[String, Boolean]): Expression
}
