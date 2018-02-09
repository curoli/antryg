package antryg.expressions

import antryg.expressions.logical.BooleanVariable
import antryg.expressions.numeric.NumericVariable

case class BinaryExpression[L, R, +T](lhs: Expression[L], op: BinaryOperator[L, R, T], rhs: Expression[R])
  extends Expression[T] {
  override def theType: Expression.Type = op.theType

  override def asString: String = {
    val lhsString = lhs match {
      case BinaryExpression(_, lhsOp, _) if lhsOp.precedence < op.precedence => s"(${lhs.asString})"
      case _ => lhs.asString
    }
    val rhsString = lhs match {
      case BinaryExpression(_, rhsOp, _) if rhsOp.precedence <= op.precedence => s"(${rhs.asString})"
      case _ => rhs.asString
    }
    s"$lhsString ${op.symbol} $rhsString"
  }

  override def valueOpt: Option[T] =
    (lhs.valueOpt, rhs.valueOpt) match {
      case (Some(lhsValue), Some(rhsValue)) => Some(op(lhsValue, rhsValue))
      case _ => None
    }

  override def variables: Set[Variable[_]] = lhs.variables ++ rhs.variables

  override def numericVariables: Set[NumericVariable] = lhs.numericVariables ++ rhs.numericVariables

  override def booleanVariables: Set[BooleanVariable] = lhs.booleanVariables ++ rhs.booleanVariables

  override def bindNumber(varName: String, value: Double): Expression[T] =
    copy(lhs = lhs.bindNumber(varName, value), rhs = rhs.bindNumber(varName, value))

  override def bindBoolean(varName: String, value: Boolean): Expression[T] =
    copy(lhs = lhs.bindBoolean(varName, value), rhs = rhs.bindBoolean(varName, value))

  override def bind(numberValues: Map[String, Double], booleanValues: Map[String, Boolean]): Expression[T] =
    copy(lhs = lhs.bind(numberValues, booleanValues), rhs = rhs.bind(numberValues, booleanValues))
}

