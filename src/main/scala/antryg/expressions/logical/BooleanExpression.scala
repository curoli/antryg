package antryg.expressions.logical

import antryg.expressions.Expression

trait BooleanExpression extends Expression[Boolean] {
  override def theType: Expression.Logical.type = Expression.Logical

  override def bindNumber(varName: String, value: Double): BooleanExpression

  override def bindBoolean(varName: String, value: Boolean): BooleanExpression

  override def bind(numberValues: Map[String, Double], booleanValues: Map[String, Boolean]): BooleanExpression

}
