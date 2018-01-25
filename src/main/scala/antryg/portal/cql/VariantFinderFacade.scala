package antryg.portal.cql

import antryg.cql.{CqlCol, CqlSession}
import antryg.cql.CqlTableSchema.PrimaryKey
import antryg.cql.builder.Replication
import antryg.cql.facade.{CqlTableFacade, KeyspaceFacade}
import VariantFinderSchema.Cols
import antryg.portal.cql.VariantFinderFacade.{VariantCohortData, VariantCoreData}
import com.datastax.driver.core.DataType

class VariantFinderFacade(val session: CqlSession, replication: Replication) {

  val keyspace: KeyspaceFacade = new KeyspaceFacade(session, VariantFinderSchema.keyspaceName, replication)
  val variantTable: CqlTableFacade =
    new CqlTableFacade(
      keyspace = keyspace,
      name = VariantFinderSchema.TableNames.variantTable,
      primaryKey = PrimaryKey(Seq(VariantFinderSchema.Cols.variantId), Seq.empty),
      otherCols = Seq(Cols.chromosome, Cols.position)
    )

  variantTable.createIfNeeded()

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
    variantTable.addCol(cohortPhenoCol)
  }


  def insertVariantCohortData(cohortData: VariantCohortData): Unit = {
    val cohortPhenoCol = getCohortPhenoCol(cohortData.cohort, cohortData.pheno)
    val row: Map[String, Any] = Map(
      Cols.variantId -> cohortData.variantId,
      cohortPhenoCol -> cohortData.values
    ).map { case (key, value) => (key.name, value) }
    variantTable.insert(row)
  }

}

object VariantFinderFacade {

  case class VariantCoreData(variantId: String, chromosome: String, position: Long)

  case class VariantCohortData(variantId: String, cohort: String, pheno: String, values: Map[String, Double])

}

