package antryg.kpql.parse

import antryg.kpql.KpqlQuery
import org.scalatest.FunSuite

class KpqlParserTest extends FunSuite {

  def assertSuccess(string: String, result: Either[String, KpqlQuery]): Unit = {
    result match {
      case Left(message) => fail(s"$message ($string)")
      case _: Right[_, _] => ()
    }
  }

  private val parser = KpqlParser

  def assertParseSucceeds(string: String): Unit = {
    assertSuccess(string, parser.parse(string))
  }

  test("parse") {
    assertParseSucceeds("T2D @ METSIM ? P_VALUE < 0.01 & ODDS_RATION > 1")
  }

}
