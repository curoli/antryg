package antryg.expressions

trait Variable[+T] extends Expression[T] {
  override def asString: String = name
  override def valueOpt: None.type = None
  def name: String
}
