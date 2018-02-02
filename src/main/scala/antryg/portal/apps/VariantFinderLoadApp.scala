package antryg.portal.apps

import antryg.cql.CqlSessionFactory
import antryg.cql.builder.Replication
import antryg.portal.apps.VariantFinderLoadApp.MenuChoice.{InfoTable, LoadCohort, LoadCore, PrintHelp, ShowTables, ShowVersions}
import antryg.portal.cql.VariantFinderFacade
import antryg.portal.sql.PortalSqlQueries.CohortPhenoTableInfo
import antryg.portal.sqltocql.{VariantFinderLoader, VariantIdSampler}
import antryg.sql.SqlDb

object VariantFinderLoadApp extends App {

  def printHelp(): Unit = {
    println(
      s"""Usage:
         |  load core   (load core variant data)
         |  load cohort <table>
         |  show versions   (show available versions)
         |  show tables <version>   (show available tables for version)
         |  info table <table>   (information about table)
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
                case "cohort" => {
                  if (args.size < 3) {
                    Left("No table specified.")
                  } else {
                    val table = args(2)
                    Right(LoadCohort(table))
                  }
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
    case Right(menuChoice) => {
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
          println(variantFinderLoader.getVersions().to[Seq].sortBy(str => str).mkString(", "))
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
        case PrintHelp => printHelp()
      }
      sqlDb.close()
      session.close()
    }
  }
  println("Bye!")
}
