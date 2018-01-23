package antryg.portal.cql

import antryg.cql.CqlSession
import antryg.cql.CqlTableSchema.PrimaryKey
import antryg.cql.builder.Replication
import antryg.cql.facade.{CqlTableFacade, KeyspaceFacade}

class VariantFinderFacade(val session: CqlSession, replication: Replication) {

  val keyspace: KeyspaceFacade = new KeyspaceFacade(session, VariantFinderSchema.keyspaceName, replication)
  val variantTable: CqlTableFacade =
    new CqlTableFacade(
      keyspace= keyspace,
      name = VariantFinderSchema.TableNames.variantTable,
      primaryKey = PrimaryKey(Seq(VariantFinderSchema.Cols.variantId), Seq.empty)
    )



}

