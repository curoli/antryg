package antryg.portal.apps

import antryg.cql.CqlSessionFactory
import antryg.cql.builder.Replication
import antryg.expressions.logical.BooleanExpression
import antryg.expressions.parse.ExpressionParser
import antryg.expressions.parse.ExpressionParser.{ParseFailure, ParseGotLogicalExpression, ParseGotNumericalExpression}
import antryg.kpql.KpqlQuery
import antryg.kpql.parse.KpqlParser
import antryg.portal.apps.VariantFinderLoadApp.MenuChoice.{InfoTable, LoadCohort, LoadCore, PrintHelp, QueryFilter, QueryKpql, QuerySimpleRange, QueryVariantCohort, ShowTables, ShowVersions}
import antryg.portal.cql.VariantFinderFacade
import antryg.portal.sql.PortalSqlQueries.CohortPhenoTableInfo
import antryg.portal.sqltocql.{VariantFinderLoader, VariantIdSampler}
import antryg.sql.SqlDb

import scala.util.{Failure, Success, Try}

object VariantFinderLoadApp extends App {

  def printHelp(): Unit = {
    println(
      s"""Usage:
         |  load core   (load core variant data)
         |  load cohort <table>
         |  show versions   (show available versions)
         |  show tables <version>   (show available tables for version)
         |  info table <table>   (information about table)
         |  query simplerange <cohort> <phenotype> <valueName> <min> <max> (finds all variants for value
         |                                                                  within range)
         |  query cohortvariants <cohort> <phenotype> <variantId>*  (look up cohort data for variants)
         |  help   (display usage)
      """.stripMargin)
  }

  def wrongArgs(message: String): Unit = {
    println(message)
    printHelp()
  }

  def tableInfoToString(tableInfo: CohortPhenoTableInfo): String =
    s"table name: ${tableInfo.tableName}, cohort: ${tableInfo.cohort}, phenotype: ${tableInfo.pheno}"

  sealed trait MenuChoice {

  }

  object MenuChoice {

    case object LoadCore extends MenuChoice

    case object ShowVersions extends MenuChoice

    case class ShowTables(version: String) extends MenuChoice

    case class InfoTable(table: String) extends MenuChoice

    case class LoadCohort(table: String) extends MenuChoice

    case class QuerySimpleRange(cohort: String, phenotype: String, valueName: String, min: Double, max: Double)
      extends MenuChoice

    case class QueryVariantCohort(cohort: String, phenotype: String, variantIds: Seq[String]) extends MenuChoice

    case class QueryFilter(cohort: String, phenotype: String, filter: BooleanExpression) extends MenuChoice

    case class QueryKpql(kpqlQuery: KpqlQuery) extends MenuChoice

    case object PrintHelp extends MenuChoice

    def fromArgs(args: Array[String]): Either[String, MenuChoice] = {
      if (args.isEmpty) {
        Left("No command specified")
      } else {
        val command = args(0)
        command match {
          case "load" =>
            if (args.size < 2) {
              Left("No source specified")
            } else {
              val source = args(1)
              source match {
                case "core" => Right(LoadCore)
                case "cohort" =>
                  if (args.size < 3) {
                    Left("No table specified.")
                  } else {
                    val table = args(2)
                    Right(LoadCohort(table))
                  }
                case _ => Left(s"Unknown source '$source'")
              }
            }
          case "show" =>
            if (args.size < 2) {
              Left("Didn't specify what to show")
            } else {
              val itemsOfInterest = args(1)
              itemsOfInterest match {
                case "versions" => Right(ShowVersions)
                case "tables" =>
                  if (args.size < 3) {
                    Left("Did not specify version.")
                  } else {
                    val version = args(2)
                    Right(ShowTables(version))
                  }
                case _ => Left(s"Don't know how to show $itemsOfInterest.")
              }
            }
          case "info" =>
            if (args.size < 2) {
              Left("Did not specify what kind of info.")
            } else {
              val itemToInfo = args(1)
              itemToInfo match {
                case "table" =>
                  if (args.size < 3) {
                    Left("Did not specify table.")
                  } else {
                    val table = args(2)
                    Right(InfoTable(table))
                  }
                case _ => Left(s"Don't know how to show info on '$itemToInfo'")
              }
            }
          case "query" =>
            if (args.size < 2) {
              Left("Did not specify what kind of query")
            } else {
              val queryType = args(1)
              queryType match {
                case "simplerange" =>
                  if (args.size < 7) {
                    Left("For simplerange query, need to specify cohort, phenotype, value name, min and max")
                  } else {
                    val cohort = args(2)
                    val phenotype = args(3)
                    val valueName = args(4)
                    val minString = args(5)
                    Try(minString.toDouble) match {
                      case Failure(ex) => Left(s"Invalid arg '$minString' for min: ${ex.getMessage}")
                      case Success(min) =>
                        val maxString = args(6)
                        Try(maxString.toDouble) match {
                          case Failure(ex) => Left(s"Invalid arg '$maxString' for max: ${ex.getMessage}")
                          case Success(max) => Right(QuerySimpleRange(cohort, phenotype, valueName, min, max))
                        }
                    }
                  }
                case "cohortvariants" =>
                  if (args.size < 5) {
                    Left("Need to specify cohort, phenotype and one or more variant ids.")
                  } else {
                    val cohort = args(2)
                    val phenotype = args(3)
                    val variantIds = args.toSeq.drop(4)
                    Right(QueryVariantCohort(cohort, phenotype, variantIds))
                  }
                case "filter" =>
                  if (args.size < 5) {
                    Left("Need to specify cohort, phenotype and filter expression")
                  } else {
                    val cohort = args(2)
                    val phenotype = args(3)
                    val filterString = args.toSeq.drop(4).mkString(" ")
                    val parser = ExpressionParser()
                    parser.parse(filterString) match {
                      case parseFailure: ParseFailure =>
                        Left(parseFailure.issues.mkString("/n", "/n", "/n"))
                      case ParseGotNumericalExpression(_) =>
                        Left("Got numerical expression, but need boolean expression.")
                      case ParseGotLogicalExpression(filter) =>
                        Right(QueryFilter(cohort, phenotype, filter))
                    }
                  }
                case "kpql" =>
                  if (args.size < 3) {
                    Left("Need to specify KPQL string.")
                  } else {
                    val kpqlString = args.toSeq.drop(2).mkString(" ")
                    KpqlParser.parse(kpqlString) match {
                      case Left(message) => Left(message)
                      case Right(kpqlQuery) => Right(QueryKpql(kpqlQuery))
                    }
                  }
                case _ => Left(s"Do not know how to do query '$queryType'")
              }
            }
          case "help" => Right(PrintHelp)
          case _ => Left(s"Unknown command '$command'")
        }
      }
    }

  }

  println("Hello!")
  println(args)
  val menuChoiceEither = MenuChoice.fromArgs(args)
  menuChoiceEither match {
    case Left(message) => wrongArgs(message)
    case Right(menuChoice) =>
      val session = CqlSessionFactory.LocalFactory.session
      val replication = Replication.SimpleStrategy(1)
      val variantFinderFacade = new VariantFinderFacade(session, replication)
      val sqlDb = SqlDb.DefaultDb
      sqlDb.setFetchSize(1000)
      val variantIdSampler = VariantIdSampler.decimateBy(1000)
      val variantFinderLoader = new VariantFinderLoader(sqlDb, variantFinderFacade, variantIdSampler)
      menuChoice match {
        case LoadCore => variantFinderLoader.loadVariantMainTable()
        case ShowVersions =>
          println(variantFinderLoader.queryVersions().to[Seq].sortBy(str => str).mkString(", "))
        case ShowTables(version) =>
          println(variantFinderLoader.getCohortPhenoTablesForVersion(version).map(tableInfoToString)
            .mkString("\n"))
        case InfoTable(table) =>
          val tableInfoOpt = variantFinderLoader.getCohortPhenoTableInfo(table)
          tableInfoOpt match {
            case Some(tableInfo) => println(tableInfoToString(tableInfo))
            case None => println(s"Could not get table info for table '$table'")
          }
        case LoadCohort(table) => variantFinderLoader.loadCohortPhenoTable(table)
        case QuerySimpleRange(cohort, phenotype, valueName, min, max) =>
          val variantsIter =
            variantFinderFacade.selectVariantsByValueRange(cohort, phenotype, valueName, min, max)
          println(s"Variants found: ${variantsIter.mkString(", ")}")
        case QueryVariantCohort(cohort, phenotype, variantIds) =>
          val variantCohortIter =
            variantFinderFacade.selectVariantsCoreCohortData(variantIds.iterator, cohort, phenotype)
          variantCohortIter.foreach(println)
        case QueryFilter(cohort, phenotype, filter) =>
          val messageOrVariantDataIter = variantFinderFacade.selectVariantsByExpression(cohort, phenotype, filter)
          messageOrVariantDataIter match {
            case Left(message) => println(message)
            case Right(variantDataIter) =>
              for (variantData <- variantDataIter) {
                println(variantData)
              }
          }
        case QueryKpql(kpqlQuery) =>
          println("KPQL queries are not yet implemented.")
          println(kpqlQuery)
        case PrintHelp => printHelp()
      }
      sqlDb.close()
      session.close()
  }
  println("Bye!")
}
