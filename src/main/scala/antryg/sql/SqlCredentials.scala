package antryg.sql

import antryg.util.conf.PropertiesTree

case class SqlCredentials(url: String, user: String, password: String) {

}

object SqlCredentials {
  val dbKey = "db"
  val urlKey = "url"
  val userKey = "user"
  val passwordKey = "password"

  def fromPropertiesDbTree(dbTree: PropertiesTree): Option[SqlCredentials] = {
    for {
      url <- dbTree.get(urlKey)
      user <- dbTree.get(userKey)
      password <- dbTree.get(passwordKey)
    } yield SqlCredentials(url, user, password)
  }

  def namedFromPropertiesTree(name: String, tree: PropertiesTree): Option[SqlCredentials] = {
    for {
      dbsTree <- tree.children.get(dbKey)
      dbTree <- dbsTree.children.get(name)
      sqlCredentials <- fromPropertiesDbTree(dbTree)
    } yield sqlCredentials
  }

  def allFromPropertiesTree(tree: PropertiesTree): Map[String, SqlCredentials] =
    tree.subTree(dbKey).children.mapValues(fromPropertiesDbTree).collect {
      case (name, Some(sqlCredentials)) => (name, sqlCredentials)
    }
}