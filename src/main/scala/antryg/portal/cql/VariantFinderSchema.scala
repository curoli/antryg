package antryg.portal.cql

import antryg.cql.CqlTableSchema.PrimaryKey
import antryg.cql.{CqlCol, CqlTableSchema}
import com.datastax.driver.core.DataType

case class VariantFinderSchema(variantTable: CqlTableSchema, variantValueIndexTable: CqlTableSchema) {

}

object VariantFinderSchema {
  val keyspaceName = "digkb"
  object TableNames {
    val variantTable = "variants"
    val variantValueIndexTable = "variantValueIndex"
  }
  object Cols {
    val variantId = CqlCol("variantId", DataType.text)
    val chromosome = CqlCol("chromosome", DataType.text)
    val position = CqlCol("position", DataType.bigint)
    val predictedEffect = CqlCol("predictedEffect", DataType.text)
    val referenceDatasets = CqlCol("referenceDatasets", DataType.set(DataType.text))
    val cohort = CqlCol("cohort", DataType.text)
    val phenotype = CqlCol("phenotype", DataType.text)
    val valueName = CqlCol("valueName", DataType.text)
    val value = CqlCol("value", DataType.cdouble)
  }
  val minimal: VariantFinderSchema = {
    val variantTable =
      CqlTableSchema(
        name = TableNames.variantTable,
        key = PrimaryKey(Seq(Cols.variantId), Seq.empty),
        otherCols = Seq(Cols.chromosome, Cols.position, Cols.predictedEffect, Cols.referenceDatasets)
      )
    val variantValueIndexTable =
      CqlTableSchema(
        name = TableNames.variantValueIndexTable,
        key = PrimaryKey(Seq(Cols.cohort, Cols.phenotype, Cols.valueName), Seq(Cols.value, Cols.variantId)),
        otherCols = Seq.empty
      )
    VariantFinderSchema(variantTable, variantValueIndexTable)
  }
}