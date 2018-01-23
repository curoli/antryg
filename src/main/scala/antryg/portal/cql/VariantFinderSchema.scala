package antryg.portal.cql

import antryg.cql.CqlTableSchema.PrimaryKey
import antryg.cql.{CqlCol, CqlTableSchema}
import com.datastax.driver.core.DataType

case class VariantFinderSchema(variantTable: CqlTableSchema) {

}

object VariantFinderSchema {
  object TableNames {
    val variantTable = "variants"
  }
  object Cols {
    val variantId = CqlCol("variantId", DataType.text)
    val chromosome = CqlCol("chromosome", DataType.text)
    val position = CqlCol("position", DataType.bigint)
    val predictedEffect = CqlCol("predictedEffect", DataType.text)
    val referenceDatasets = CqlCol("referenceDatasets", DataType.set(DataType.text))
  }
  def empty: VariantFinderSchema = {
    val variantTable =
      CqlTableSchema(
        name = TableNames.variantTable,
        key = PrimaryKey(Seq(Cols.variantId), Seq.empty),
        otherCols = Seq(Cols.chromosome, Cols.position, Cols.predictedEffect, Cols.referenceDatasets)
      )
    VariantFinderSchema(variantTable)
  }
}