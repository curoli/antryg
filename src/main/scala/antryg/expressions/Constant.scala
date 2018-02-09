package antryg.expressions

trait Constant[+T] extends Expression[T] {
  override def asString: String = value.toString

  override def valueOpt: Option[T] = Some(value)

  def value: T
}
