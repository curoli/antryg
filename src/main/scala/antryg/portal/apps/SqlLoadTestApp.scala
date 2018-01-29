package antryg.portal.apps

import scalikejdbc.{ConnectionPool, DB, scalikejdbcSQLInterpolationImplicitDef}


object SqlLoadTestApp extends App {
  val url = "XXX"
  val user = "XXX"
  val password = "XXX"
  ConnectionPool.singleton(url, user, password)
  println("Submitting query")
  DB.readOnly { implicit session =>
    sql"select VAR_ID, CHROM, POS from common_dv1".fetchSize(100).foreach(println)
  }
}
