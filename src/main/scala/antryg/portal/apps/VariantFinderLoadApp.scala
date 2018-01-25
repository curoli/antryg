package antryg.portal.apps

import antryg.cql.CqlSessionFactory
import antryg.cql.builder.Replication
import antryg.portal.cql.VariantFinderFacade
import antryg.portal.sqltocql.{VariantFinderLoader, VariantIdSampler}
import antryg.sql.SqlDb

object VariantFinderLoadApp extends App {
  val memoryMonitor = new Runnable {
    override def run(): Unit = {
      while(true) {
        val rt = Runtime.getRuntime
        println(s"Memory: max: ${rt.maxMemory()}  free: ${rt.freeMemory()}  total: ${rt.totalMemory()}")
        Thread.sleep(10000)
      }
    }
  }
  (new Thread(memoryMonitor)).start()
  val session = CqlSessionFactory.LocalFactory.session
  val replication = Replication.SimpleStrategy(1)
  val variantFinderFacade = new VariantFinderFacade(session, replication)
  val sqlDb = SqlDb.DefaultDb
  val variantIdSampler = VariantIdSampler.decimateBy(1234)
  val variantFinderLoader = new VariantFinderLoader(sqlDb, variantFinderFacade, variantIdSampler)
  println("yo")
  variantFinderLoader.load()
  println("snurck")
  sqlDb.close()
  session.close()
}
