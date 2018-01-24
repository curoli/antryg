package antryg.portal.sqltocql

import java.util.Date

import antryg.portal.cql.VariantFinderFacade
import antryg.portal.sql.PortalSqlQueries
import antryg.portal.sqltocql.VariantFinderLoader.Reporter
import antryg.sql.SqlDb

class VariantFinderLoader(sqlDb: SqlDb, variantFinderFacade: VariantFinderFacade, variantIdSampler: VariantIdSampler,
                          reporter: Reporter = Reporter.TimeIntervalReporter(10000)) {

  def loadVariantMainTable(): Unit = {
    reporter.sendingQueryToSql()
    val rows = sqlDb.queryReadOnly(PortalSqlQueries.selectVariantCoreData())
    reporter.sendingInsertsToCassandra()
    var count: Long = 0L
    rows.foreach { row =>
      if (variantIdSampler(row.variantId)) {
        variantFinderFacade.insertVariantCoreData(row)
        count += 1
        reporter.reportCoreDataLoaded(count)
      }
    }
    reporter.doneLoadingCoreData()
  }

  def load(): Unit = {
    loadVariantMainTable()
  }

}

object VariantFinderLoader {

  trait Reporter {
    def sendingQueryToSql(): Unit

    def sendingInsertsToCassandra(): Unit

    def reportCoreDataLoaded(count: Long): Unit

    def doneLoadingCoreData(): Unit
  }

  object Reporter {

    case class TimeIntervalReporter(interval: Long) extends Reporter {
      var lastTime: Long = System.currentTimeMillis()
      var lastCount: Long = 0L

      override def sendingQueryToSql(): Unit = {
        lastTime = System.currentTimeMillis()
        println(s"[${new Date(lastTime)}] Sending query to SQL DB.")
      }

      override def sendingInsertsToCassandra(): Unit = {
        lastTime = System.currentTimeMillis()
        println(s"[${new Date(lastTime)}] Now writing data to Cassandra.")
      }


      override def reportCoreDataLoaded(count: Long): Unit = {
        lastCount = count
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTime > interval) {
          lastTime = currentTime
          println(s"[${new Date(lastTime)}] Have loaded core data of $count variants.")
        }
      }

      override def doneLoadingCoreData(): Unit = {
        lastTime = System.currentTimeMillis()
        println(s"[${new Date(lastTime)}] Done loading core data - loaded $lastCount variants.")
      }

    }

  }

}
