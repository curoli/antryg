package antryg.expressions.numeric

import antryg.expressions.{Expression, Variable}
import antryg.expressions.logical.BooleanVariable

case class NumericVariable(name: String) extends Variable[Double] with NumericExpression {
  override def variables: Set[Variable[_]] = Set[Variable[_]](this)

  override def numericVariables: Set[NumericVariable] = Set[NumericVariable](this)

  override def booleanVariables: Set[BooleanVariable] = Set.empty

  override def bindNumber(varName: String, value: Double): NumericExpression =
    if(varName == name) {
      NumericConstant(value)
    } else {
      this
    }

  override def bindBoolean(varName: String, value: Boolean): NumericVariable = this

  override def bind(numberValues: Map[String, Double], booleanValues: Map[String, Boolean]): NumericExpression =
    numberValues.get(name) match {
      case Some(value) => NumericConstant(value)
      case None => this
    }
}
