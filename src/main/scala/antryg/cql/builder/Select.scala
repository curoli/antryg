package antryg.cql.builder

import antryg.cql.builder.Select.{AllCols, CertainCols, Clause, SelectedCols}
import antryg.expressions.BinaryExpression.NumericalComparisonExpression
import antryg.expressions.BinaryOperator.NumericalComparisonOperator
import antryg.expressions.numeric.{NumericConstant, NumericVariable}
import com.datastax.driver.core.querybuilder.{QueryBuilder, Clause => JClause, Select => JSelect}

case class Select(keyspace: String, table: String, selectedCols: SelectedCols,
                  clauses: Seq[Clause]) extends CqlStatement {
  override def asJava: JSelect.Where = {
    val selectClause = selectedCols match {
      case AllCols => QueryBuilder.select().all()
      case CertainCols(names) =>
        var selectClause = QueryBuilder.select()
        for (name <- names) {
          selectClause = selectClause.column(name)
        }
        selectClause
    }
    val selectFrom = selectClause.from(keyspace, table)
    val statement = if (clauses.isEmpty) {
      selectFrom.where()
    } else {
      var selectFromWhere = selectFrom.where(clauses.head.toJClause)
      for (clause <- clauses.tail) {
        selectFromWhere = selectFromWhere.and(clause.toJClause)
      }
      selectFromWhere
    }
    statement
  }
}

object Select {

  sealed trait SelectedCols

  object AllCols extends SelectedCols

  case class CertainCols(names: Seq[String]) extends SelectedCols

  object CertainCols {
    def apply(name: String, names: String*): CertainCols = CertainCols(name +: names)
  }

  sealed trait Clause {
    def toJClause: JClause
  }

  sealed trait BinaryClause extends Clause {
    def column: String

    def withColumn(columnNew: String): BinaryClause

    def value: Any
  }

  object BinaryClause {
    def fromOperator(varName: String, op: NumericalComparisonOperator, value: Double): BinaryClause = {
      op match {
        case NumericalComparisonOperator.equalTo => Equals(varName, value)
        case NumericalComparisonOperator.lessThan => LessThan(varName, value)
        case NumericalComparisonOperator.lessOrEqual => LessOrEqualsTo(varName, value)
        case NumericalComparisonOperator.greaterThan => GreaterThan(varName, value)
        case NumericalComparisonOperator.greaterOrEqual => GreaterOrEqualsTo(varName, value)
      }
    }

    def fromComparisonExpression(comparisonExpression: NumericalComparisonExpression): Option[BinaryClause] = {
      comparisonExpression match {
        case NumericalComparisonExpression(NumericVariable(varName), op, NumericConstant(value)) =>
          Some(fromOperator(varName, op, value))
        case NumericalComparisonExpression(NumericConstant(value), op, NumericVariable(varName)) =>
          Some(fromOperator(varName, op.reverted, value))
        case _ => None
      }
    }
  }

  case class Equals(column: String, value: Any) extends BinaryClause {
    override def toJClause: JClause = QueryBuilder.eq(column, value)

    override def withColumn(columnNew: String): Equals = copy(column = columnNew)
  }

  case class LessThan(column: String, value: Any) extends BinaryClause {
    override def toJClause: JClause = QueryBuilder.lt(column, value)

    override def withColumn(columnNew: String): LessThan = copy(column = columnNew)
  }

  case class LessOrEqualsTo(column: String, value: Any) extends BinaryClause {
    override def toJClause: JClause = QueryBuilder.lte(column, value)

    override def withColumn(columnNew: String): LessOrEqualsTo = copy(column = columnNew)
  }

  case class GreaterThan(column: String, value: Any) extends BinaryClause {
    override def toJClause: JClause = QueryBuilder.gt(column, value)

    override def withColumn(columnNew: String): GreaterThan = copy(column = columnNew)
  }

  case class GreaterOrEqualsTo(column: String, value: Any) extends BinaryClause {
    override def toJClause: JClause = QueryBuilder.gte(column, value)

    override def withColumn(columnNew: String): GreaterOrEqualsTo = copy(column = columnNew)
  }

  case class In(column: String, values: Seq[Any]) extends Clause {
    override def toJClause: JClause = QueryBuilder.in(column, values.map(_.asInstanceOf[AnyRef]).toArray: _*)
  }

}