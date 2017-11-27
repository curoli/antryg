package antryg.sql

import java.nio.file.Paths

import antryg.util.conf.PropertiesTree
import org.scalatest.FunSuite
import scalikejdbc.{ConnectionPool, DB, scalikejdbcSQLInterpolationImplicitDef}

class SqlTest extends FunSuite {

  test("Connect to DB") {
    val dbsConfFile = Paths.get("src", "test", "secrets", "dbs.conf")
    val propertiesTree = PropertiesTree.fromFile(dbsConfFile)
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
