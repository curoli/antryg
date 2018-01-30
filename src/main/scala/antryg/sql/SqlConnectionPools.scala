package antryg.sql

import java.nio.file.{Path, Paths}

import antryg.util.conf.PropertiesTree
import scalikejdbc.ConnectionPool

object SqlConnectionPools {

  val dbsConfFile: Path = Paths.get("src", "test", "secrets", "dbs.conf")

  val defaultDbName = "dev"

  def addPropsToUrl(url:String): String = url + "?useCursorFetch=true"

  val dbNames: Set[String] = {
    val propertiesTree = PropertiesTree.fromFile(dbsConfFile)
    val dbsCreds = SqlCredentials.allFromPropertiesTree(propertiesTree)
    val dbCreds = dbsCreds(defaultDbName)
    ConnectionPool.singleton(addPropsToUrl(dbCreds.url), dbCreds.user, dbCreds.password)
    dbsCreds.foreach {
      case (name, creds) => ConnectionPool.add(name, addPropsToUrl(creds.url), creds.user, creds.password)
    }
    dbsCreds.keySet
  }

  def init(): Unit = {} // No-op: just makes sure this object is loaded.

}
