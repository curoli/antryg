package antryg.cql.builder

import antryg.cql.builder.Select.{AllCols, CertainCols, Clause, SelectedCols}
import com.datastax.driver.core.querybuilder.{QueryBuilder, Clause => JClause, Select => JSelect}

case class Select(keyspace: String, table: String, selectedCols: SelectedCols,
                  clauses: Seq[Clause]) extends CqlStatement {
  override def asJava: JSelect.Where = {
    val selectClause = selectedCols match {
      case AllCols => QueryBuilder.select().all()
      case CertainCols(names) =>
        var selectClause = QueryBuilder.select()
        for(name <- names) {
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

    def value: Any
  }

  case class Equals(column: String, value: Any) extends BinaryClause {
    override def toJClause: JClause = QueryBuilder.eq(column, value)
  }

  case class LessThan(column: String, value: Any) extends BinaryClause {
    override def toJClause: JClause = QueryBuilder.lt(column, value)
  }

  case class LessOrEqualsTo(column: String, value: Any) extends BinaryClause {
    override def toJClause: JClause = QueryBuilder.lte(column, value)
  }

  case class GreaterThan(column: String, value: Any) extends BinaryClause {
    override def toJClause: JClause = QueryBuilder.gt(column, value)
  }

  case class GreaterOrEqualsTo(column: String, value: Any) extends BinaryClause {
    override def toJClause: JClause = QueryBuilder.gte(column, value)
  }

  case class In(column: String, values: Seq[Any]) extends Clause {
    override def toJClause: JClause = QueryBuilder.in(column, values.map(_.asInstanceOf[AnyRef]).toArray: _*)
  }

}