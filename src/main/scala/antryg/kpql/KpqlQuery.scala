package antryg.kpql

import antryg.expressions.logical.BooleanExpression

case class KpqlQuery(phenotype: String, datasets: Seq[String], filter: BooleanExpression) {

}
