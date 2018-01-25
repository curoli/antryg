package antryg.portal.sqltocql

import java.util.Date

import antryg.portal.cql.VariantFinderFacade
import antryg.portal.sql.PortalSqlQueries
import antryg.portal.sqltocql.VariantFinderLoader.Reporter
import antryg.portal.sqltocql.VariantFinderLoader.Reporter.{CoreTranche, Tranche}
import antryg.sql.SqlDb

class VariantFinderLoader(sqlDb: SqlDb, variantFinderFacade: VariantFinderFacade, variantIdSampler: VariantIdSampler,
                          reporter: Reporter = Reporter.TimeIntervalReporter(10000)) {

  def loadVariantMainTable(): Unit = {
    reporter.sendingCoreDataQueryToSql()
    val rows = sqlDb.queryReadOnly(PortalSqlQueries.selectVariantCoreData(Some(200000)))
    reporter.sendingCoreDataInsertsToCassandra()
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

  def loadCohortPhenoTable(table: String, cohort: String, pheno: String): Unit = {

  }

  def load(): Unit = {
    loadVariantMainTable()
  }

}

object VariantFinderLoader {

  trait Reporter {
    def sendingCoreDataQueryToSql(tranche: Tranche = CoreTranche): Unit

    def sendingCoreDataInsertsToCassandra(tranche: Tranche = CoreTranche): Unit

    def reportCoreDataLoaded(count: Long, tranche: Tranche = CoreTranche): Unit

    def doneLoadingCoreData(tranche: Tranche = CoreTranche): Unit
  }

  object Reporter {

    trait Tranche {
      def label: String
    }

    object CoreTranche extends Tranche {
      override def label: String = "core data"
    }

    case class CohortPhenoTranche(cohort: String, pheno: String) extends Tranche {
      override def label: String = s"$cohort $pheno cohort data"
    }

    case class TimeIntervalReporter(interval: Long) extends Reporter {
      var lastTime: Long = System.currentTimeMillis()
      var lastCount: Long = 0L

      override def sendingCoreDataQueryToSql(tranche: Tranche = CoreTranche): Unit = {
        lastTime = System.currentTimeMillis()
        println(s"[${new Date(lastTime)}] Sending ${tranche.label} query to SQL DB.")
      }

      override def sendingCoreDataInsertsToCassandra(tranche: Tranche = CoreTranche): Unit = {
        lastTime = System.currentTimeMillis()
        println(s"[${new Date(lastTime)}] Now writing ${tranche.label} to Cassandra.")
      }


      override def reportCoreDataLoaded(count: Long, tranche: Tranche = CoreTranche): Unit = {
        lastCount = count
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTime > interval) {
          lastTime = currentTime
          println(s"[${new Date(lastTime)}] Have loaded ${tranche.label} of $count variants.")
        }
      }

      override def doneLoadingCoreData(tranche: Tranche = CoreTranche): Unit = {
        lastTime = System.currentTimeMillis()
        println(s"[${new Date(lastTime)}] Done loading ${tranche.label} - loaded $lastCount variants.")
      }

    }

  }

}
