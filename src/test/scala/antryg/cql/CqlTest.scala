package antryg.cql

import com.datastax.driver.core.Cluster
import org.scalatest.FunSuite
import antryg.util.GoogleCommonsAdapters.GoogleCommonsListenableFutureAdapter
import scala.concurrent.ExecutionContext.Implicits.global

import scala.util.{Failure, Success}

class CqlTest extends FunSuite {

  test("CQL") {
    println("We gonna do some CQL!")
    val cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
    val sessionFut = cluster.connectAsync().asScala
    val resultSetFut = sessionFut.flatMap(_.executeAsync("select release_version from system.local").asScala)
    val versionFut = resultSetFut.map(_.one().getString("release_version"))
    versionFut.onComplete{
      case Success(version) => println(version)
      case Failure(throwable) => println(throwable.getMessage)
    }
  }

}
