package antryg.expressions.logical

import antryg.expressions.Expression

trait BooleanExpression extends Expression[Boolean] {
  override def theType: Expression.Logical.type = Expression.Logical
}
