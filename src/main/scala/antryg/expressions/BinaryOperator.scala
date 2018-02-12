package antryg.expressions

trait BinaryOperator[L, R, +T] extends ((L, R) => T) with BinaryOperator.Base {
  override def apply(lhs: L, rhs: R): T

  override def theType: Expression.Type

  override def symbol: String

  override def precedence: Int

  override def toString: String = symbol
}

object BinaryOperator {

  trait Base {
    def theType: Expression.Type

    def symbol: String

    def precedence: Int

    override def toString: String = symbol
  }

  object Precedences {
    val timesDivideBy = 5
    val plusMinus = 4
    val andOr = 3
    val comparison = 2
  }

  trait ArithmeticOperator extends BinaryOperator[Double, Double, Double] {
    def theType: Expression.Type = Expression.numeric
  }

  object ArithmeticOperator {
    val times: ArithmeticOperator = new ArithmeticOperator {
      override def symbol: String = "*"

      override def apply(lhs: Double, rhs: Double): Double = lhs * rhs

      override def precedence: Int = Precedences.timesDivideBy
    }
    val dividedBy: ArithmeticOperator = new ArithmeticOperator {
      override def symbol: String = "/"

      override def apply(lhs: Double, rhs: Double): Double = lhs / rhs

      override def precedence: Int = Precedences.timesDivideBy
    }
    val plus: ArithmeticOperator = new ArithmeticOperator {
      override def symbol: String = "+"

      override def apply(lhs: Double, rhs: Double): Double = lhs + rhs

      override def precedence: Int = Precedences.plusMinus
    }
    val minus: ArithmeticOperator = new ArithmeticOperator {
      override def symbol: String = "-"

      override def apply(lhs: Double, rhs: Double): Double = lhs - rhs

      override def precedence: Int = Precedences.plusMinus
    }
  }

  trait BooleanOperator extends BinaryOperator[Boolean, Boolean, Boolean] {
    def theType: Expression.Type = Expression.logical

    override def precedence: Int = Precedences.andOr
  }

  object BooleanOperator {
    val and: BooleanOperator = new BooleanOperator {
      override def symbol: String = "&"

      override def apply(lhs: Boolean, rhs: Boolean): Boolean = lhs && rhs
    }
    val or: BooleanOperator = new BooleanOperator {
      override def symbol: String = "|"

      override def apply(lhs: Boolean, rhs: Boolean): Boolean = lhs || rhs
    }
  }

  trait NumericalComparisonOperator extends BinaryOperator[Double, Double, Boolean] {
    def theType: Expression.Type = Expression.logical

    override def precedence: Int = Precedences.comparison
  }

  object NumericalComparisonOperator {
    val equalTo: NumericalComparisonOperator = new NumericalComparisonOperator {
      override def symbol: String = "="

      override def apply(lhs: Double, rhs: Double): Boolean = lhs == rhs
    }
    val lessThan: NumericalComparisonOperator = new NumericalComparisonOperator {
      override def symbol: String = "<"

      override def apply(lhs: Double, rhs: Double): Boolean = lhs < rhs
    }
    val lessOrEqual: NumericalComparisonOperator = new NumericalComparisonOperator {
      override def symbol: String = "<="

      override def apply(lhs: Double, rhs: Double): Boolean = lhs <= rhs
    }
    val greaterThan: NumericalComparisonOperator = new NumericalComparisonOperator {
      override def symbol: String = ">"

      override def apply(lhs: Double, rhs: Double): Boolean = lhs > rhs
    }
    val greaterOrEqual: NumericalComparisonOperator = new NumericalComparisonOperator {
      override def symbol: String = ">="

      override def apply(lhs: Double, rhs: Double): Boolean = lhs >= rhs
    }
  }

}