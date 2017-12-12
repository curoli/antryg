package antryg.sqltocql

import antryg.sql.SqlType
import com.datastax.driver.core.DataType

object SqlToCql {

  type TypeConvert = PartialFunction[SqlType, DataType]

  object TypeConverters {
    val default: TypeConvert = {
      case sqlType if sqlType.isVarSizeType =>
        val baseName = sqlType.varSizeBaseNameOpt.get
        val size = sqlType.varSizeOpt.get
        baseName match {
          case SqlType.VarSizeTypes.int =>
            if (size <= 2) DataType.tinyint
            else if (size <= 4) DataType.smallint
            else if (size <= 9) DataType.cint
            else if (size <= 19) DataType.bigint
            else DataType.varint
          case SqlType.VarSizeTypes.varchar => DataType.text
        }
      case SqlType.text | SqlType.longtext => DataType.text
      case SqlType.float => DataType.cfloat
      case SqlType.double => DataType.cdouble
    }

    val allToText: TypeConvert = {
      case _ => DataType.text
    }

    val defaultWithTextFallback: TypeConvert = default.orElse(allToText)

  }

}
