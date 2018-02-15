package antryg.expressions.transform

import antryg.expressions.Expression

trait ExpressionTransformer {

  def transform(expression: Expression.Base): Expression.Base

}
