package antryg.expressions.logical

import antryg.expressions.{Expression, Variable}
import antryg.expressions.numeric.NumericVariable

case class BooleanVariable(name: String) extends Variable[Boolean] {
  override def variables: Set[Variable[_]] = Set[Variable[_]](this)

  override def numericVariables: Set[NumericVariable] = Set.empty

  override def booleanVariables: Set[BooleanVariable] = Set(this)

  override def bindNumber(varName: String, value: Double): Expression[Boolean] = this

  override def bindBoolean(varName: String, value: Boolean): Expression[Boolean] =
    if (varName == name) {
      BooleanConstant(value)
    } else {
      this
    }

  override def bind(numberValues: Map[String, Double], booleanValues: Map[String, Boolean]): Expression[Boolean] =
    booleanValues.get(name) match {
      case Some(value) => BooleanConstant(value)
      case None => this
    }
}
