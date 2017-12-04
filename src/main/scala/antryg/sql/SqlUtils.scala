package antryg.sql

import scalikejdbc.{DB, HasExtractor, NamedDB, SQLToResult}
import scala.language.higherKinds

object SqlUtils {
  def withDefaultDB[A, C[_]](query: SQLToResult[A, HasExtractor, C]): C[A] =
    DB.readOnly { implicit session => query.apply() }

  def withNamedDB[A, C[_]](name: String, query: SQLToResult[A, HasExtractor, C]): C[A] =
    NamedDB(name).readOnly { implicit session => query.apply() }

}
