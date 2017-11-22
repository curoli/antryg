package antryg.util

import scala.language.reflectiveCalls

object Closer {
  def using[A, R <: {def close() : Unit}](closable: R)(expr: R => A): A = {
    val a = expr(closable)
    closable.close()
    a
  }
}
