package antryg.expressions.numeric

import antryg.expressions.Expression

trait NumericExpression extends Expression[Double] {
  override def theType: Expression.Numeric.type = Expression.Numeric

  override def bindNumber(varName: String, value: Double): NumericExpression

  override def bindBoolean(varName: String, value: Boolean): NumericExpression

  override def bind(numberValues: Map[String, Double], booleanValues: Map[String, Boolean]): NumericExpression

}
