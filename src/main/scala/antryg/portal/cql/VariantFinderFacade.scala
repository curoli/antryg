package antryg.portal.cql

import antryg.cql.{CqlCol, CqlSession}
import antryg.cql.CqlTableSchema.PrimaryKey
import antryg.cql.builder.Replication
import antryg.cql.facade.{CqlTableFacade, KeyspaceFacade}
import VariantFinderSchema.Cols
import antryg.portal.cql.VariantFinderFacade.VariantCoreData

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
    ).map { case (key, value) => (key.name, value )}
    variantTable.insert(values)
  }

}

object VariantFinderFacade {
  case class VariantCoreData(variantId: String, chromosome: String, position: Long)
}

