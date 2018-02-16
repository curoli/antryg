package antryg.portal.cql

import antryg.cql.{CqlCol, CqlSession}
import antryg.cql.CqlTableSchema.PrimaryKey
import antryg.cql.builder.{Replication, Select}
import antryg.cql.facade.{CqlTableFacade, KeyspaceFacade}
import VariantFinderSchema.Cols
import antryg.cql.builder.Select.Clause
import antryg.expressions.logical.BooleanExpression
import antryg.portal.cql.VariantFinderFacade.{RowToVariantCoreCohortData, VariantCohortData, VariantCoreCohortData, VariantCoreCohortDataFilter, VariantCoreData}
import com.datastax.driver.core.{DataType, Row}
import com.datastax.driver.core.exceptions.InvalidQueryException

import scala.collection.JavaConverters.mapAsScalaMapConverter
import scala.collection.JavaConverters.asScalaIteratorConverter

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

  def addCohortPhenoCol(cohort: String, pheno: String): Unit = {
    val cohortPhenoCol = VariantFinderFacade.getCohortPhenoCol(cohort, pheno)
    try {
      variantTable.addCol(cohortPhenoCol)
    } catch {
      case ex: InvalidQueryException if ex.getMessage.contains("conflicts with an existing column") => ()
    }
  }


  def insertVariantCohortData(cohortData: VariantCohortData): Unit = {
    val cohortPhenoCol = VariantFinderFacade.getCohortPhenoCol(cohortData.cohort, cohortData.phenotype)
    val variantRow: Map[String, Any] = Map(
      Cols.variantId -> cohortData.variantId,
      cohortPhenoCol -> cohortData.values
    ).map { case (key, value) => (key.name, value) }
    variantTable.insert(variantRow)
    for ((valueName, value) <- cohortData.values) {
      val variantValueIndexRow: Map[String, Any] = Map(
        Cols.cohort -> cohortData.cohort,
        Cols.phenotype -> cohortData.phenotype,
        Cols.valueName -> valueName,
        Cols.value -> value,
        Cols.variantId -> cohortData.variantId
      ).map { case (key, rowValue) => (key.name, rowValue) }
      variantValueIndexTable.insert(variantValueIndexRow)
    }
  }

  def getRequiredVariantIndexClauses(cohort: String, phenotype: String, valueName: String): Seq[Clause] = {
    val cohortClause = Select.Equals(Cols.cohort.name, cohort)
    val phenotypeClause = Select.Equals(Cols.phenotype.name, phenotype)
    val valueNameClause = Select.Equals(Cols.valueName.name, valueName)
    Seq(cohortClause, phenotypeClause, valueNameClause)
  }

  def selectVariantsByValueRange(cohort: String, phenotype: String, valueName: String,
                                 min: Double, max: Double): Iterator[String] = {
    val minClause = Select.GreaterOrEqualsTo(Cols.value.name, min)
    val maxClause = Select.LessOrEqualsTo(Cols.value.name, max)
    val clauses = getRequiredVariantIndexClauses(cohort, phenotype, valueName) ++ Seq(minClause, maxClause)
    val resultSet = variantValueIndexTable.select(Select.CertainCols(Cols.variantId.name), clauses)
    resultSet.iterator().asScala.map(_.getString(Cols.variantId.name))
  }

  val variantIdGroupSize: Int = 10

  def selectVariantsCoreCohortData(variantIdIter: Iterator[String], cohort: String,
                                   phenotype: String): Iterator[VariantCoreCohortData] = {
    val cohortPhenoCol = VariantFinderFacade.getCohortPhenoCol(cohort, phenotype)
    val selectedCols =
      Select.CertainCols(Cols.variantId.name, Cols.chromosome.name, Cols.position.name, cohortPhenoCol.name)
    val variantIdGroupIter = variantIdIter.grouped(variantIdGroupSize)
    variantIdGroupIter.flatMap { variantIdGroup =>
      val variantIdGroupClause = Select.In(Cols.variantId.name, variantIdGroup)
      val resultSet = variantTable.select(selectedCols, Seq(variantIdGroupClause))
      resultSet.iterator().asScala.map(RowToVariantCoreCohortData(cohort, phenotype))
    }
  }

  val queryStrategy: VariantFinderQueryStrategy.type = VariantFinderQueryStrategy

  def selectVariantsByExpression(cohort: String, phenotype: String, filter: BooleanExpression):
  Either[String, Iterator[VariantCoreCohortData]] = {
    queryStrategy.createPlan(filter) match {
      case Left(message) => Left(message)
      case Right(plan) =>
        println(plan.valueFilters.toSeq)
        val clauses = getRequiredVariantIndexClauses(cohort, phenotype, plan.valueName) ++ plan.valueFilters.toSeq
        val resultSet = variantValueIndexTable.select(Select.CertainCols(Cols.variantId.name), clauses)
        val variantIdIter = resultSet.iterator().asScala.map(_.getString(Cols.variantId.name))
        val variantDataIter =
          selectVariantsCoreCohortData(variantIdIter, cohort, phenotype)
            .filter(VariantCoreCohortDataFilter(filter))
        Right(variantDataIter)
    }
  }

}

object VariantFinderFacade {

  case class VariantCoreData(variantId: String, chromosome: String, position: Long)

  case class VariantCohortData(variantId: String, cohort: String, phenotype: String, values: Map[String, Double])

  case class VariantCoreCohortData(variantId: String, chromosome: String, position: Long,
                                   cohort: String, phenotype: String, values: Map[String, Double])

  def getCohortPhenoCol(cohort: String, phenotype: String): CqlCol =
    CqlCol(s"dataset_${cohort}__${phenotype}", DataType.map(DataType.text, DataType.cdouble()))


  case class RowToVariantCoreCohortData(cohort: String, phenotype: String) extends (Row => VariantCoreCohortData) {
    val cohortPhenoCol: CqlCol = getCohortPhenoCol(cohort, phenotype)
    override def apply(row: Row): VariantCoreCohortData = {
      val variantId = row.getString(Cols.variantId.name)
      val chromosome = row.getString(Cols.chromosome.name)
      val position = row.getLong(Cols.position.name)
      val values =
        row.getMap[String, java.lang.Double](cohortPhenoCol.name, classOf[String], classOf[java.lang.Double])
          .asScala.toMap.asInstanceOf[Map[String, Double]]
      VariantCoreCohortData(variantId, chromosome, position, cohort, phenotype, values)
    }
  }

  case class VariantCoreCohortDataFilter(filter: BooleanExpression) extends (VariantCoreCohortData => Boolean) {
    override def apply(variantData: VariantCoreCohortData): Boolean = {
      filter.bind(variantData.values, Map.empty).valueOpt.getOrElse(false)
    }
  }

}

