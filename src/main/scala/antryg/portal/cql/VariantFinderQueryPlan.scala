package antryg.portal.cql

import antryg.cql.builder.Select.BinaryClause
import antryg.expressions.logical.BooleanExpression

case class VariantFinderQueryPlan(valueName: String, valueFilters: Set[BinaryClause], filter: BooleanExpression) {

}
