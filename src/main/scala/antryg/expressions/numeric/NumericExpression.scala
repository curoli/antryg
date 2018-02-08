package antryg.expressions.numeric

import antryg.expressions.Expression

trait NumericExpression extends Expression {
  override def bindNumber(varName: String, value: Double): NumericExpression

  override def bindBoolean(varName: String, value: Boolean): NumericExpression

  override def bind(numberValues: Map[String, Double], booleanValues: Map[String, Boolean]): NumericExpression
}
