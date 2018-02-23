package antryg.kpql.parse

import antryg.kpql.KpqlQuery
import org.scalatest.FunSuite

class KpqlParserTest extends FunSuite {

  def assertSuccess(string: String, result: Either[String, KpqlQuery], phenotype: String,
                    datasets: Seq[String]): Unit = {
    result match {
      case Left(message) => fail(s"$message ($string)")
      case Right(query) =>
        assert(query.phenotype === phenotype)
        assert(query.datasets === datasets)
    }
  }

  private val parser = KpqlParser

  def assertParseSucceeds(string: String, phenotype: String, datasets: Seq[String]): Unit = {
    assertSuccess(string, parser.parse(string), phenotype, datasets)
  }

  test("parse") {
    assertParseSucceeds("T2D @ METSIM ? P_VALUE < 0.01 & ODDS_RATION > 1", "T2D", Seq("METSIM"))
    assertParseSucceeds("T2D @ METSIM, BioME ? P_VALUE < 0.01 & ODDS_RATION > 1", "T2D", Seq("METSIM", "BioME"))
  }

}
