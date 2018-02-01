package antryg.portal.apps

import antryg.cql.CqlSessionFactory
import antryg.cql.builder.Replication
import antryg.portal.apps.VariantFinderLoadApp.MenuChoice.{LoadCohort, LoadCore, PrintHelp, ShowMetDataVersions}
import antryg.portal.cql.VariantFinderFacade
import antryg.portal.sqltocql.{VariantFinderLoader, VariantIdSampler}
import antryg.sql.SqlDb

object VariantFinderLoadApp extends App {
  def printHelp(): Unit = {
    println(
      s"""Usage:
         |  load core (load core variant data)
      """.stripMargin)
  }
  def wrongArgs(message: String): Unit = {
    println(message)
    printHelp()
  }

  sealed trait MenuChoice {

  }

  object MenuChoice {

    case object LoadCore extends MenuChoice
    case object ShowMetDataVersions extends MenuChoice
    case class LoadCohort(cohort: String, pheno: String) extends MenuChoice
    case object PrintHelp extends MenuChoice

    def fromArgs(args: Array[String]): Either[String, MenuChoice] = {
      if(args.isEmpty) {
        Left("No command specified")
      } else {
        val command = args(0)
        command match {
          case "load" =>
            if(args.size < 2) {
             Left("No source specified")
            } else {
              val source = args(1)
              source match {
                case "core" => Right(LoadCore)
                case "cohort" => {
                  if(args.size < 4) {
                    Left("Did not specify cohort and phenotype.")
                  } else {
                    val cohort = args(2)
                    val pheno = args(3)
                    Right(LoadCohort(cohort, pheno))
                  }
                }
                case _ => Left(s"Unknown source '$source'")
              }
            }
          case "show" =>
            if(args.size < 2) {
              Left("Didn't specify what to show")
            } else {
              val itemsOfInterest = args(1)
              itemsOfInterest match {
                case "versions" => Right(ShowMetDataVersions)
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
        case ShowMetDataVersions =>
          println(variantFinderLoader.getMetaDataVersions().to[Seq].sortBy(str => str).mkString(", "))
        case LoadCohort(cohort, pheno) => ???
        case PrintHelp => printHelp()
      }
      sqlDb.close()
      session.close()
    }
  }
  println("Bye!")
}
