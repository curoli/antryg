package antryg.portal.sqltocql

import java.util.Date

import antryg.portal.cql.VariantFinderFacade
import antryg.portal.cql.VariantFinderFacade.VariantCoreData
import antryg.portal.sql.{PortalSqlQueries, PortalSqlSchema}
import antryg.portal.sqltocql.VariantFinderLoader.Reporter
import antryg.portal.sqltocql.VariantFinderLoader.Reporter.{CoreTranche, Tranche}
import antryg.sql.SqlDb
import scalikejdbc.WrappedResultSet

class VariantFinderLoader(sqlDb: SqlDb, variantFinderFacade: VariantFinderFacade, variantIdSampler: VariantIdSampler,
                          reporter: Reporter = Reporter.TimeIntervalReporter(10000)) {

  def loadVariantMainTable(): Unit = {
    reporter.sendingCoreDataQueryToSql(CoreTranche)
    val selectLimitOpt = Some(20000000)
    reporter.sendingCoreDataInsertsToCassandra(CoreTranche)
    var count: Long = 0L
    val visitor: WrappedResultSet => Unit = { row =>
      val coreData = PortalSqlSchema.getVariantCoreData(row)
      if (variantIdSampler(coreData.variantId)) {
        variantFinderFacade.insertVariantCoreData(coreData)
        count += 1
        reporter.reportCoreDataLoaded(count, CoreTranche)
      }
    }
    sqlDb.queryReadOnlyForeach(PortalSqlQueries.selectVariantCoreData(selectLimitOpt), visitor)
    reporter.doneLoadingCoreData(CoreTranche)
  }

  def getMetaDataVersions(): Set[String] = sqlDb.queryReadOnly(PortalSqlQueries.selectMetaDataVersions).toSet

  def loadCohortPhenoTable(table: String, cohort: String, pheno: String): Unit = {

  }

  def load(): Unit = {
    loadVariantMainTable()
  }

}

object VariantFinderLoader {

  trait Reporter {
    def sendingCoreDataQueryToSql(tranche: Tranche): Unit

    def sendingCoreDataInsertsToCassandra(tranche: Tranche): Unit

    def reportCoreDataLoaded(count: Long, tranche: Tranche): Unit

    def doneLoadingCoreData(tranche: Tranche): Unit
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

      override def sendingCoreDataQueryToSql(tranche: Tranche): Unit = {
        lastTime = System.currentTimeMillis()
        println(s"[${new Date(lastTime)}] Sending ${tranche.label} query to SQL DB.")
      }

      override def sendingCoreDataInsertsToCassandra(tranche: Tranche): Unit = {
        lastTime = System.currentTimeMillis()
        println(s"[${new Date(lastTime)}] Now writing ${tranche.label} to Cassandra.")
      }


      override def reportCoreDataLoaded(count: Long, tranche: Tranche): Unit = {
        lastCount = count
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTime > interval) {
          lastTime = currentTime
          println(s"[${new Date(lastTime)}] Have loaded ${tranche.label} of $count variants.")
        }
      }

      override def doneLoadingCoreData(tranche: Tranche): Unit = {
        lastTime = System.currentTimeMillis()
        println(s"[${new Date(lastTime)}] Done loading ${tranche.label} - loaded $lastCount variants.")
      }

    }

  }

}
