package antryg.sqltocql

import antryg.cql.CqlType
import antryg.sql.SqlType

object SqlToCql {

  def convert(sqlType: SqlType): CqlType = {
    if(sqlType.isVarSizeType) {
      val baseName = sqlType.varSizeBaseNameOpt.get
      val size = sqlType.varSizeOpt.get
      baseName match {
        case "int" =>
          if(size <= 2) CqlType.tinyint
          else if(size <= 4) CqlType.smallint
          else if(size <= 9) CqlType.int
          else if(size <= 19) CqlType.bigint
          else CqlType.varint
      }
    } else {
      sqlType match {
        case SqlType("longtext") => CqlType("text")
      }
    }
  }

}
