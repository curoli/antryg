package antryg.portal.sql

import antryg.portal.cql.VariantFinderFacade.VariantCoreData
import antryg.sql.{SqlCol, SqlTableSchema}
import scalikejdbc.WrappedResultSet

object PortalSqlSchema {

  val samplesTable = "SAMPLE"

  object CommonCols {
    val variantId: String = "VAR_ID"
  }

  object VariantMainTable {
    val name: String = "common_dv1"
    object Cols {
      val variantId: String = CommonCols.variantId
      val chromosome: String = "CHROM"
      val position: String = "POS"
      val coreCols: Seq[String] = Seq(variantId, chromosome, position)
    }
  }

  def getVariantColumn(schema: SqlTableSchema): Option[SqlCol] = schema.cols.find(_.name == CommonCols.variantId)

  def getVariantCoreData(row: WrappedResultSet): VariantCoreData = {
    val variantId = row.string(VariantMainTable.Cols.variantId)
    val chromosome = row.string(VariantMainTable.Cols.chromosome)
    val position = row.long(VariantMainTable.Cols.position)
    VariantCoreData(variantId, chromosome, position)
  }

}
