package antryg.expressions.numeric

import antryg.expressions.Expression

trait NumericExpression extends Expression[Double] {
  override def theType: Expression.Numeric.type = Expression.Numeric
}
