package antryg.expressions

import antryg.expressions.logical.BooleanVariable
import antryg.expressions.numeric.NumericVariable

trait Expression[+T] extends Expression.Base {
  override def valueOpt: Option[T]

  override def bindNumber(varName: String, value: Double): Expression[T]

  override def bindBoolean(varName: String, value: Boolean): Expression[T]

  override def bind(numberValues: Map[String, Double], booleanValues: Map[String, Boolean]): Expression[T]
}

object Expression {

  trait Type

  object Numeric extends Type
  object Logical extends Type

  trait Base {
    def theType: Type

    def asString: String

    def valueOpt: Option[Any]

    def variables: Set[Variable[_]]

    def numericVariables: Set[NumericVariable]

    def booleanVariables: Set[BooleanVariable]

    def bindNumber(varName: String, value: Double): Base

    def bindBoolean(varName: String, value: Boolean): Base

    def bind(numberValues: Map[String, Double], booleanValues: Map[String, Boolean]): Base

    def as[T]: Expression[T] = this.asInstanceOf[Expression[T]]
  }
}
