package antryg.expressions.numeric

import antryg.expressions.{Expression, Variable}
import antryg.expressions.logical.BooleanVariable

case class NumericVariable(name: String) extends Variable[Double] {
  override def theType: Expression.Type = Expression.numeric

  override def variables: Set[Variable[_]] = Set[Variable[_]](this)

  override def numericVariables: Set[NumericVariable] = Set[NumericVariable](this)

  override def booleanVariables: Set[BooleanVariable] = Set.empty

  override def bindNumber(varName: String, value: Double): Expression[Double] =
    if(varName == name) {
      NumericConstant(value)
    } else {
      this
    }

  override def bindBoolean(varName: String, value: Boolean): NumericVariable = this

  override def bind(numberValues: Map[String, Double], booleanValues: Map[String, Boolean]): Expression[Double] =
    numberValues.get(name) match {
      case Some(value) => NumericConstant(value)
      case None => this
    }
}
