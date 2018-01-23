package antryg.cql.facade

import antryg.cql.CqlSession
import antryg.cql.builder.{CreateKeyspace, DropKeyspace, Replication}

class KeyspaceFacade(val session: CqlSession, val name: String, val replication: Replication) {

  var exists: Boolean = false

  def create(): Unit = {
      session.execute(CreateKeyspace(name, ifNotExists = true, replication))
      exists = true
  }

  def createIfNeeded(): Unit = if(!exists) create()

  def drop(): Unit = {
      session.execute(DropKeyspace(name, ifExist = true))
      exists = false
  }

  def dropIfNeeded(): Unit = if(exists) drop()

}
