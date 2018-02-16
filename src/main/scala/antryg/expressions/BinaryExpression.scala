package antryg.expressions

import antryg.expressions.BinaryOperator.{ArithmeticOperator, BooleanOperator, NumericalComparisonOperator}
import antryg.expressions.logical.{BooleanExpression, BooleanVariable}
import antryg.expressions.numeric.{NumericExpression, NumericVariable}

trait BinaryExpression[L, R, +T] extends Expression[T] {
  def lhs: Expression[L]

  def rhs: Expression[R]

  def op: BinaryOperator[L, R, T]

  override def theType: Expression.Type = op.theType

  override def asString: String = {
    val lhsString = lhs match {
      case binLhs: BinaryExpression[_, _, _] if binLhs.op.precedence < op.precedence => s"(${lhs.asString})"
      case _ => lhs.asString
    }
    val rhsString = lhs match {
      case binRhs: BinaryExpression[_, _, _] if binRhs.op.precedence <= op.precedence => s"(${rhs.asString})"
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

  override def bindNumber(varName: String, value: Double): Expression[T]

  override def bindBoolean(varName: String, value: Boolean): Expression[T]

  override def bind(numberValues: Map[String, Double], booleanValues: Map[String, Boolean]): Expression[T]
}

object BinaryExpression {
  def create(lhs: Expression.Base, op: BinaryOperator.Base, rhs: Expression.Base):
  Either[String, Expression.Base] = {
    (lhs, op, rhs) match {
      case (numLhs: NumericExpression, numOp: ArithmeticOperator, numRhs: NumericExpression) =>
        Right(ArithmeticBinaryExpression(numLhs, numOp, numRhs))
      case (boolLhs: BooleanExpression, boolOp: BooleanOperator, boolRhs: BooleanExpression) =>
        Right(BooleanBinaryExpression(boolLhs, boolOp, boolRhs))
      case (numLhs: NumericExpression, compOp: NumericalComparisonOperator, numRhs: NumericExpression) =>
        Right(NumericalComparisonExpression(numLhs, compOp, numRhs))
      case _ => Left("Cannot create binary expression from $lhs, $op, rhs")
    }
  }

  case class ArithmeticBinaryExpression(rhs: NumericExpression, op: ArithmeticOperator, lhs: NumericExpression)
    extends BinaryExpression[Double, Double, Double] with NumericExpression {
    override def bindNumber(varName: String, value: Double): ArithmeticBinaryExpression =
      copy(lhs = lhs.bindNumber(varName, value), rhs = rhs.bindNumber(varName, value))

    override def bindBoolean(varName: String, value: Boolean): ArithmeticBinaryExpression =
      copy(lhs = lhs.bindBoolean(varName, value), rhs = rhs.bindBoolean(varName, value))

    override def bind(numberValues: Map[String, Double], booleanValues: Map[String, Boolean]):
    ArithmeticBinaryExpression =
      copy(lhs = lhs.bind(numberValues, booleanValues), rhs = rhs.bind(numberValues, booleanValues))
  }

  case class BooleanBinaryExpression(rhs: BooleanExpression, op: BooleanOperator, lhs: BooleanExpression)
    extends BinaryExpression[Boolean, Boolean, Boolean] with BooleanExpression {
    override def bindNumber(varName: String, value: Double): BooleanBinaryExpression =
      copy(lhs = lhs.bindNumber(varName, value), rhs = rhs.bindNumber(varName, value))

    override def bindBoolean(varName: String, value: Boolean): BooleanBinaryExpression =
      copy(lhs = lhs.bindBoolean(varName, value), rhs = rhs.bindBoolean(varName, value))

    override def bind(numberValues: Map[String, Double], booleanValues: Map[String, Boolean]):
    BooleanBinaryExpression =
      copy(lhs = lhs.bind(numberValues, booleanValues), rhs = rhs.bind(numberValues, booleanValues))
  }

  case class NumericalComparisonExpression(lhs: NumericExpression, op: NumericalComparisonOperator,
                                           rhs: NumericExpression)
    extends BinaryExpression[Double, Double, Boolean] with BooleanExpression {
    override def bindNumber(varName: String, value: Double): NumericalComparisonExpression =
      copy(lhs = lhs.bindNumber(varName, value), rhs = rhs.bindNumber(varName, value))

    override def bindBoolean(varName: String, value: Boolean): NumericalComparisonExpression =
      copy(lhs = lhs.bindBoolean(varName, value), rhs = rhs.bindBoolean(varName, value))

    override def bind(numberValues: Map[String, Double], booleanValues: Map[String, Boolean]):
    NumericalComparisonExpression =
      copy(lhs = lhs.bind(numberValues, booleanValues), rhs = rhs.bind(numberValues, booleanValues))
  }
}

