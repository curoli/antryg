package antryg.sqltocql

import antryg.cql.CqlTableSchema.PrimaryKey
import antryg.cql.{CqlCol, CqlTableSchema}
import antryg.sql.{SqlCol, SqlTableSchema, SqlType}
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
          case SqlType.VarSizeTypes.bigint => DataType.bigint
          case SqlType.VarSizeTypes.varchar => DataType.text
        }
      case SqlType.text | SqlType.longtext => DataType.text
      case SqlType.float => DataType.cfloat
      case SqlType.double => DataType.cdouble
      case SqlType.bigint => DataType.bigint
      case SqlType.datetime => DataType.date
      case SqlType.timestamp => DataType.date
    }

    val allToText: TypeConvert = {
      case _ => DataType.text
    }

    val defaultWithTextFallback: TypeConvert = default.orElse(allToText)

  }

  def convertCol(sqlCol: SqlCol, typeConvert: TypeConvert): CqlCol =
    CqlCol(sqlCol.name, typeConvert(sqlCol.sqlType))

  def convertTable(sqlTableSchema: SqlTableSchema, typeConvert: TypeConvert, partitionColNames: Seq[String],
                   clusterColNames: Seq[String]): CqlTableSchema = {
    val cqlCols = sqlTableSchema.cols.map(convertCol(_, typeConvert))
    val cqlColsByName = cqlCols.map(col => (col.name, col)).toMap
    val partitionCols = partitionColNames.map(cqlColsByName)
    val clusterCols = clusterColNames.map(cqlColsByName)
    val partitionAndClusterNames = partitionColNames.toSet ++ clusterColNames.toSet
    val otherCols = cqlCols.filterNot(col => partitionAndClusterNames(col.name))
    val primaryKey = PrimaryKey(partitionCols, clusterCols)
    CqlTableSchema(sqlTableSchema.name, primaryKey, otherCols)
  }

}
