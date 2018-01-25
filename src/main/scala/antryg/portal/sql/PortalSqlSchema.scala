package antryg.portal.sql

import antryg.sql.{SqlCol, SqlTableSchema}

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

}
