package antryg.kpql

import antryg.expressions.logical.BooleanExpression
import antryg.util.{Combinator, MapOfSets}

trait KpqlFilterEvaluator {

  def evaluate(filter: BooleanExpression, bindings: Iterable[Map[String, Double]]): Boolean

}

object KpqlFilterEvaluator {

  def bindingFilter(filter: BooleanExpression): Map[String, Double] => Boolean =
    binding => filter.bind(binding, Map.empty).valueOpt.getOrElse(false)

  val oneMatches: KpqlFilterEvaluator = (filter: BooleanExpression, bindings: Iterable[Map[String, Double]]) => {
    bindings.exists(bindingFilter(filter))
  }

  val allMatch: KpqlFilterEvaluator = (filter: BooleanExpression, bindings: Iterable[Map[String, Double]]) => {
    bindings.forall(bindingFilter(filter))
  }

  val mixedMatches: KpqlFilterEvaluator = (filter: BooleanExpression, bindings: Iterable[Map[String, Double]]) => {
    MapOfSets.mix(bindings).exists(bindingFilter(filter))
  }
}
