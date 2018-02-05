package antryg.portal.cql

import antryg.cql.{CqlCol, CqlSession}
import antryg.cql.CqlTableSchema.PrimaryKey
import antryg.cql.builder.Replication
import antryg.cql.facade.{CqlTableFacade, KeyspaceFacade}
import VariantFinderSchema.Cols
import antryg.portal.cql.VariantFinderFacade.{VariantCohortData, VariantCoreData}
import com.datastax.driver.core.DataType
import com.datastax.driver.core.exceptions.InvalidQueryException

class VariantFinderFacade(val session: CqlSession, replication: Replication) {

  val keyspace: KeyspaceFacade = new KeyspaceFacade(session, VariantFinderSchema.keyspaceName, replication)
  val variantTable: CqlTableFacade =
    CqlTableFacade(keyspace, VariantFinderSchema.minimal.variantTable)
  val variantValueIndexTable: CqlTableFacade =
    CqlTableFacade(keyspace, VariantFinderSchema.minimal.variantValueIndexTable)

  variantTable.createIfNeeded()
  variantValueIndexTable.createIfNeeded()

  def insertVariantCoreData(coreData: VariantCoreData): Unit = {
    val values = Map(
      Cols.variantId -> coreData.variantId,
      Cols.chromosome -> coreData.chromosome,
      Cols.position -> coreData.position
    ).map { case (key, value) => (key.name, value) }
    variantTable.insert(values)
  }

  def getCohortPhenoCol(cohort: String, pheno: String): CqlCol =
    CqlCol(s"dataset_${cohort}__${pheno}", DataType.map(DataType.text, DataType.cdouble()))


  def addCohortPhenoCol(cohort: String, pheno: String): Unit = {
    val cohortPhenoCol = getCohortPhenoCol(cohort, pheno)
    try {
      variantTable.addCol(cohortPhenoCol)
    } catch {
      case ex: InvalidQueryException if ex.getMessage.contains("conflicts with an existing column") => ()
    }
  }


  def insertVariantCohortData(cohortData: VariantCohortData): Unit = {
    val cohortPhenoCol = getCohortPhenoCol(cohortData.cohort, cohortData.pheno)
    val variantRow: Map[String, Any] = Map(
      Cols.variantId -> cohortData.variantId,
      cohortPhenoCol -> cohortData.values
    ).map { case (key, value) => (key.name, value) }
    variantTable.insert(variantRow)
    for ((valueName, value) <- cohortData.values) {
      val variantValueIndexRow: Map[String, Any] = Map(
        Cols.cohort -> cohortData.cohort,
        Cols.phenotype -> cohortData.pheno,
        Cols.valueName -> valueName,
        Cols.value -> value,
        Cols.variantId -> cohortData.variantId
      ).map { case (key, rowValue) => (key.name, rowValue) }
      variantValueIndexTable.insert(variantValueIndexRow)
    }
  }

}

object VariantFinderFacade {

  case class VariantCoreData(variantId: String, chromosome: String, position: Long)

  case class VariantCohortData(variantId: String, cohort: String, pheno: String, values: Map[String, Double])

}

