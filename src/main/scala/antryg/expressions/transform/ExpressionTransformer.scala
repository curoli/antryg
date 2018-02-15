package antryg.expressions.transform

import antryg.expressions.Expression
import antryg.expressions.logical.BooleanExpression
import antryg.expressions.numeric.NumericExpression

trait ExpressionTransformer {

  def transformNumeric(expression: NumericExpression): NumericExpression

  def transformBoolean(expression: BooleanExpression): BooleanExpression

  def transform(expression: Expression.Base): Expression.Base = expression match {
    case numExp: NumericExpression => transformNumeric(numExp)
    case boolExp: BooleanExpression => transformBoolean(boolExp)
    case _ => expression
  }

}
