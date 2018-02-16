package antryg.expressions

trait BinaryOperator[L, R, +T] extends ((L, R) => T) with BinaryOperator.Base {
  override def apply(lhs: L, rhs: R): T

  override def theType: Expression.Type

  override def lhsType: Expression.Type

  override def rhsType: Expression.Type

  override def symbol: String

  override def precedence: Int

  override def toString: String = symbol
}

trait ReversibleOperator[L, R, +T] extends BinaryOperator[L, R, T] {
  def reverted: ReversibleOperator[R, L, T]
}

object BinaryOperator {

  trait Base {
    def theType: Expression.Type

    def lhsType: Expression.Type

    def rhsType: Expression.Type

    def symbol: String

    def precedence: Int

    override def toString: String = symbol

    def as[L, R, T]: BinaryOperator[L, R, T] = this.asInstanceOf[BinaryOperator[L, R, T]]
  }

  object Precedences {
    val timesDivideBy = 5
    val plusMinus = 4
    val comparison = 3
    val andOr = 2
  }

  sealed trait ArithmeticOperator extends BinaryOperator[Double, Double, Double] {
    override def theType: Expression.Type = Expression.Numeric

    override def lhsType: Expression.Type = Expression.Numeric

    override def rhsType: Expression.Type = Expression.Numeric
  }

  sealed trait ReversibleArithmeticOperator
    extends ArithmeticOperator with ReversibleOperator[Double, Double, Double] {
    override def reverted: ReversibleArithmeticOperator
  }

  object ArithmeticOperator {
    val times: ArithmeticOperator = new ReversibleArithmeticOperator {
      override def symbol: String = "*"

      override def apply(lhs: Double, rhs: Double): Double = lhs * rhs

      override def precedence: Int = Precedences.timesDivideBy

      override def reverted: ReversibleArithmeticOperator = this
    }
    val dividedBy: ArithmeticOperator = new ArithmeticOperator {
      override def symbol: String = "/"

      override def apply(lhs: Double, rhs: Double): Double = lhs / rhs

      override def precedence: Int = Precedences.timesDivideBy
    }
    val plus: ArithmeticOperator = new ReversibleArithmeticOperator {
      override def symbol: String = "+"

      override def apply(lhs: Double, rhs: Double): Double = lhs + rhs

      override def precedence: Int = Precedences.plusMinus

      override def reverted: ReversibleArithmeticOperator = this
    }
    val minus: ArithmeticOperator = new ArithmeticOperator {
      override def symbol: String = "-"

      override def apply(lhs: Double, rhs: Double): Double = lhs - rhs

      override def precedence: Int = Precedences.plusMinus
    }
  }

  trait BooleanOperator extends ReversibleOperator[Boolean, Boolean, Boolean] {
    override def theType: Expression.Type = Expression.Logical

    override def lhsType: Expression.Type = Expression.Logical

    override def rhsType: Expression.Type = Expression.Logical

    override def precedence: Int = Precedences.andOr

    override def reverted: BooleanOperator
  }

  object BooleanOperator {
    val and: BooleanOperator = new BooleanOperator {
      override def symbol: String = "&"

      override def apply(lhs: Boolean, rhs: Boolean): Boolean = lhs && rhs

      override def reverted: BooleanOperator = this
    }
    val or: BooleanOperator = new BooleanOperator {
      override def symbol: String = "|"

      override def apply(lhs: Boolean, rhs: Boolean): Boolean = lhs || rhs

      override def reverted: BooleanOperator = this
    }
  }

  trait NumericalComparisonOperator extends ReversibleOperator[Double, Double, Boolean] {
    override def theType: Expression.Type = Expression.Logical

    override def lhsType: Expression.Type = Expression.Numeric

    override def rhsType: Expression.Type = Expression.Numeric

    override def precedence: Int = Precedences.comparison

    override def reverted: NumericalComparisonOperator
  }

  object NumericalComparisonOperator {
    val equalTo: NumericalComparisonOperator = new NumericalComparisonOperator {
      override def symbol: String = "="

      override def apply(lhs: Double, rhs: Double): Boolean = lhs == rhs

      override def reverted: NumericalComparisonOperator = this
    }
    val lessThan: NumericalComparisonOperator = new NumericalComparisonOperator {
      override def symbol: String = "<"

      override def apply(lhs: Double, rhs: Double): Boolean = lhs < rhs

      override def reverted: NumericalComparisonOperator = greaterThan
    }
    val lessOrEqual: NumericalComparisonOperator = new NumericalComparisonOperator {
      override def symbol: String = "<="

      override def apply(lhs: Double, rhs: Double): Boolean = lhs <= rhs

      override def reverted: NumericalComparisonOperator = greaterOrEqual
    }
    val greaterThan: NumericalComparisonOperator = new NumericalComparisonOperator {
      override def symbol: String = ">"

      override def apply(lhs: Double, rhs: Double): Boolean = lhs > rhs

      override def reverted: NumericalComparisonOperator = lessThan
    }
    val greaterOrEqual: NumericalComparisonOperator = new NumericalComparisonOperator {
      override def symbol: String = ">="

      override def apply(lhs: Double, rhs: Double): Boolean = lhs >= rhs

      override def reverted: NumericalComparisonOperator = lessOrEqual
    }
  }

}