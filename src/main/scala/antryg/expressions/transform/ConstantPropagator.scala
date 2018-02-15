package antryg.expressions.transform

import antryg.expressions.BinaryExpression.{ArithmeticBinaryExpression, BooleanBinaryExpression, NumericalComparisonExpression}
import antryg.expressions.BinaryOperator.{ArithmeticOperator, BooleanOperator}
import antryg.expressions.logical.{BooleanConstant, BooleanExpression}
import antryg.expressions.numeric.{NumericConstant, NumericExpression}

object ConstantPropagator extends ExpressionTransformer {

  override def transformNumeric(expression: NumericExpression): NumericExpression = expression match {
    case ArithmeticBinaryExpression(lhs, op, rhs) =>
      (transformNumeric(lhs), op, transformNumeric(rhs)) match {
        case (NumericConstant(lhsValue), _, NumericConstant(rhsValue)) => NumericConstant(op(lhsValue, rhsValue))
        case (NumericConstant(0.0), ArithmeticOperator.plus, rhsNew) => rhsNew
        case (lhsNew, ArithmeticOperator.plus, NumericConstant(0.0)) => lhsNew
        case (lhsNew, ArithmeticOperator.minus, NumericConstant(0.0)) => lhsNew
        case (NumericConstant(1.0), ArithmeticOperator.times, rhsNew) => rhsNew
        case (lhsNew, ArithmeticOperator.times, NumericConstant(1.0)) => lhsNew
        case (lhsNew, ArithmeticOperator.dividedBy, NumericConstant(1.0)) => lhsNew
        case (lhsNew, _, rhsNew) => ArithmeticBinaryExpression(lhsNew, op, rhsNew)
      }
    case _ => expression
  }

  override def transformBoolean(expression: BooleanExpression): BooleanExpression = expression match {
    case NumericalComparisonExpression(lhs, op, rhs) =>
      (transformNumeric(lhs), transformNumeric(rhs)) match {
        case (NumericConstant(lhsValue), NumericConstant(rhsValue)) => BooleanConstant(op(lhsValue, rhsValue))
        case (lhsNew, rhsNew) => NumericalComparisonExpression(lhsNew, op, rhsNew)
      }
    case BooleanBinaryExpression(lhs, op, rhs) =>
      (transformBoolean(lhs), op, transformBoolean(rhs)) match {
        case (BooleanConstant(true), BooleanOperator.and, BooleanConstant(true)) => BooleanConstant(true)
        case (BooleanConstant(false), BooleanOperator.and, _) => BooleanConstant(false)
        case (_, BooleanOperator.and, BooleanConstant(false)) => BooleanConstant(false)
        case (BooleanConstant(false), BooleanOperator.or, BooleanConstant(false)) => BooleanConstant(false)
        case (BooleanConstant(true), BooleanOperator.or, _) => BooleanConstant(true)
        case (_, BooleanOperator.or, BooleanConstant(true)) => BooleanConstant(true)
        case (lhsNew, _, rhsNew) => BooleanBinaryExpression(lhsNew, op, rhsNew)
      }
  }
}
