package antryg.sql

import java.nio.file.{Path, Paths}

import antryg.util.conf.PropertiesTree
import scalikejdbc.{ConnectionPool, DB}

object SqlConnectionPools {

  val dbsConfFile: Path = Paths.get("src", "test", "secrets", "dbs.conf")

  val defaultDbName = "dev"

  val dbNames: Set[String] = {
    val propertiesTree = PropertiesTree.fromFile(dbsConfFile)
    val dbsCreds = SqlCredentials.allFromPropertiesTree(propertiesTree)
    val dbCreds = dbsCreds(defaultDbName)
    ConnectionPool.singleton(dbCreds.url, dbCreds.user, dbCreds.password)
    dbsCreds.foreach {
      case (name, creds) => ConnectionPool.add(name, creds.url, creds.user, creds.password)
    }
    dbsCreds.keySet
  }

  def init(): Unit = {} // No-op: just makes sure this object is loaded.

}
