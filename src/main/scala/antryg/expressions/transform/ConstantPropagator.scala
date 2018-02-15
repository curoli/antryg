package antryg.expressions.transform
import antryg.expressions.{BinaryExpression, Expression}

object ConstantPropagator extends ExpressionTransformer {
  override def transform(expression: Expression.Base): Expression.Base = {
    expression match {
      case BinaryExpression(lhs, op, rhs) =>
        val lhsNew = transform(lhs)
        val rhsNew = transform(rhs)
        (lhsNew, rhsNew) match {
          case _ => ???
        }
    }
  }
}
