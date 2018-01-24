package antryg.portal.apps

import antryg.cql.CqlSessionFactory
import antryg.cql.builder.Replication
import antryg.portal.cql.VariantFinderFacade
import antryg.portal.sqltocql.{VariantFinderLoader, VariantIdSampler}
import antryg.sql.SqlDb

object VariantFinderLoadApp extends App {
  val session = CqlSessionFactory.LocalFactory.session
  val replication = Replication.SimpleStrategy(1)
  val variantFinderFacade = new VariantFinderFacade(session, replication)
  val sqlDb = SqlDb.DefaultDb
  val variantIdSampler = VariantIdSampler.decimateBy(123456)
  val variantFinderLoader = new VariantFinderLoader(sqlDb, variantFinderFacade, variantIdSampler)
  println("yo")
  variantFinderLoader.load()
  println("snurck")
  sqlDb.close()
  session.close()
}
