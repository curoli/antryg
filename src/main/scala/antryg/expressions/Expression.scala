package antryg.expressions

import antryg.expressions.logical.BooleanVariable
import antryg.expressions.numeric.NumericVariable

trait Expression[+T] {
  def asString: String

  def valueOpt: Option[T]

  def variables: Set[Variable[_]]

  def numericVariables: Set[NumericVariable]

  def booleanVariables: Set[BooleanVariable]

  def bindNumber(varName: String, value: Double): Expression[T]

  def bindBoolean(varName: String, value: Boolean): Expression[T]

  def bind(numberValues: Map[String, Double], booleanValues: Map[String, Boolean]): Expression[T]
}
