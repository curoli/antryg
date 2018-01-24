package antryg.portal.sqltocql

import antryg.portal.cql.VariantFinderFacade
import antryg.portal.cql.VariantFinderSchema.{Cols => CqlCols}
import antryg.portal.sql.PortalSqlQueries
import antryg.portal.sql.PortalSqlSchema.VariantMainTable.{Cols => SqlCols}
import antryg.portal.sqltocql.VariantFinderLoader.Reporter
import antryg.sql.SqlDb

class VariantFinderLoader(sqlDb: SqlDb, variantFinderFacade: VariantFinderFacade, variantIdSampler: VariantIdSampler,
                          reporter: Reporter = Reporter.TimeIntervalReporter(10000)) {

  val mainTableColsMap: Map[String, String] = Map(
    SqlCols.variantId -> CqlCols.variantId,
    SqlCols.chromosome -> CqlCols.chromosome,
    SqlCols.position -> CqlCols.position
  ).mapValues(_.name)

  def loadVariantMainTable(): Unit = {
    val rows = sqlDb.queryReadOnly(PortalSqlQueries.selectVariantCoreData())
    rows.foreach { row =>
      if (variantIdSampler(row.variantId)) {
        variantFinderFacade.insertVariantCoreData(row)
      }
    }
  }

  def load(): Unit = {
    loadVariantMainTable()
  }

}

object VariantFinderLoader {

  trait Reporter {
    def startLoadingCoreData(): Unit

    def reportCoreDataLoaded(count: Long): Unit

    def doneLoadingCoreData(): Unit
  }

  object Reporter {

    case class TimeIntervalReporter(interval: Long) extends Reporter {
      var lastTime: Long = System.currentTimeMillis()
      var lastCount: Long = 0L
      override def startLoadingCoreData(): Unit = {
        println("Start loading core data")
        lastTime = System.currentTimeMillis()
      }

      override def reportCoreDataLoaded(count: Long): Unit = {
        val currentTime = System.currentTimeMillis()
        if(currentTime - lastTime > interval) {
          println(s"Have loaded core data of $count variants.")
          lastCount = count
          lastTime = currentTime
        }
      }

      override def doneLoadingCoreData(): Unit = {
        println(s"Done loading core data - loaded $lastCount variants.")
      }
    }

  }

}
