package antryg.portal.sqltocql

import java.util.Date

import antryg.portal.cql.VariantFinderFacade
import antryg.portal.cql.VariantFinderFacade.VariantCohortData
import antryg.portal.sql.PortalSqlQueries.CohortPhenoTableInfo
import antryg.portal.sql.{PortalSqlQueries, PortalSqlSchema}
import antryg.portal.sqltocql.VariantFinderLoader.Reporter
import antryg.portal.sqltocql.VariantFinderLoader.Reporter.{CohortPhenoTranche, CoreTranche, Tranche}
import antryg.sql.SqlDb
import scalikejdbc.WrappedResultSet

class VariantFinderLoader(sqlDb: SqlDb, variantFinderFacade: VariantFinderFacade,
                          variantIdSampler: VariantIdSampler,
                          reporter: Reporter = Reporter.TimeIntervalReporter(10000)) {

  def loadVariantMainTable(): Unit = {
    reporter.sendingDataQueryToSql(CoreTranche)
    val selectLimitOpt = Some(20000000)
    reporter.sendingDataInsertsToCassandra(CoreTranche)
    var count: Long = 0L
    val visitor: WrappedResultSet => Unit = { row =>
      val coreData = PortalSqlSchema.getVariantCoreData(row)
      if (variantIdSampler(coreData.variantId)) {
        variantFinderFacade.insertVariantCoreData(coreData)
        count += 1
        reporter.reportDataLoaded(count, CoreTranche)
      }
    }
    sqlDb.queryReadOnlyForeach(PortalSqlQueries.selectVariantCoreData(selectLimitOpt), visitor)
    reporter.doneLoadingData(CoreTranche)
  }

  def getVersions(): Set[String] = sqlDb.queryReadOnly(PortalSqlQueries.selectMetaDataVersions).toSet

  def getCohortPhenoTablesForVersion(version: String): Seq[CohortPhenoTableInfo] =
    sqlDb.queryReadOnly(PortalSqlQueries.selectCohortPhenoByVersion(version)).toList

  def getCohortPhenoTableInfo(table: String): Option[CohortPhenoTableInfo] =
    sqlDb.queryReadOnly(PortalSqlQueries.selectCohortPhenoByTable(table))

  def loadCohortPhenoTable(table: String): Unit = {
    val tableInfoOpt = getCohortPhenoTableInfo(table)
    tableInfoOpt match {
      case Some(tableInfo) => loadCohortPhenoTable(table, tableInfo.cohort, tableInfo.pheno)
      case None => reporter.error(s"Could not get info for table '$table'")
    }
  }

  def loadCohortPhenoTable(table: String, cohort: String, pheno: String): Unit = {
    val tranche = CohortPhenoTranche(cohort, pheno)
    reporter.sendingDataQueryToSql(tranche)
    reporter.sendingDataInsertsToCassandra(tranche)
    var count: Long = 0L
    sqlDb.queryReadOnly(PortalSqlQueries.selectFromCohortPhenoTable(table)).foreach { row =>
      val variantId = row.variantId
      if(variantIdSampler(variantId)) {
        val variantCohortData = VariantCohortData(row.variantId, cohort, pheno, row.values)
        variantFinderFacade.insertVariantCohortData(variantCohortData)
        count += 1
        reporter.reportDataLoaded(count, tranche)
      }
    }
    reporter.doneLoadingData(tranche)
  }

  def load(): Unit = {
    loadVariantMainTable()
  }

}

object VariantFinderLoader {

  trait Reporter {
    def error(message: String): Unit

    def sendingDataQueryToSql(tranche: Tranche): Unit

    def sendingDataInsertsToCassandra(tranche: Tranche): Unit

    def reportDataLoaded(count: Long, tranche: Tranche): Unit

    def doneLoadingData(tranche: Tranche): Unit
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

      override def error(message: String): Unit = println(message)

      override def sendingDataQueryToSql(tranche: Tranche): Unit = {
        lastTime = System.currentTimeMillis()
        println(s"[${new Date(lastTime)}] Sending ${tranche.label} query to SQL DB.")
      }

      override def sendingDataInsertsToCassandra(tranche: Tranche): Unit = {
        lastTime = System.currentTimeMillis()
        println(s"[${new Date(lastTime)}] Now writing ${tranche.label} to Cassandra.")
      }


      override def reportDataLoaded(count: Long, tranche: Tranche): Unit = {
        lastCount = count
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTime > interval) {
          lastTime = currentTime
          println(s"[${new Date(lastTime)}] Have loaded ${tranche.label} of $count variants.")
        }
      }

      override def doneLoadingData(tranche: Tranche): Unit = {
        lastTime = System.currentTimeMillis()
        println(s"[${new Date(lastTime)}] Done loading ${tranche.label} - loaded $lastCount variants.")
      }

    }

  }

}
