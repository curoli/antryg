package antryg.cql

import antryg.util.GoogleCommonsAdapters.GoogleCommonsListenableFutureAdapter
import org.scalatest.FunSuite

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class CqlTest extends FunSuite {

  test("CQL") {
    val session = CqlSessionFactory.LocalFactory.session
    val resultSetFut = session.executeAsync("select release_version from system.local").asScala
    val versionFut = resultSetFut.map(_.one().getString("release_version"))
    versionFut.onComplete{
      case Success(version) => println(version)
      case Failure(throwable) => println(throwable.getMessage)
    }
  }

}
