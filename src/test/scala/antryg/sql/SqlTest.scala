package antryg.sql

import java.nio.file.{Files, Paths}
import java.util.Properties

import antryg.util.conf.PropertiesTree
import org.scalatest.FunSuite
import scalikejdbc.{ConnectionPool, DB, _}

class SqlTest extends FunSuite {

  test("Connect to DB") {
    val dbsConfFile = Paths.get("src", "test", "secrets", "dbs.conf")
    val properties = new Properties
    properties.load(Files.newInputStream(dbsConfFile))
    val propertiesTree = PropertiesTree.fromProperties(properties)
    val dbsCreds = SqlCredentials.allFromPropertiesTree(propertiesTree)
    val dbName = "dev"
    val dbCreds = dbsCreds(dbName)
    ConnectionPool.singleton(dbCreds.url, dbCreds.user, dbCreds.password)
    println(dbCreds.url)
    val dbs = DB.readOnly { implicit session =>
      sql"show tables".map(_.string("Tables_in_dig_dev")).list.apply
    }
    println(dbs)
  }

}
