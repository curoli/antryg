package antryg.sql

import org.scalatest.FunSuite

class SqlTypeTest extends FunSuite {
  test("Parse var size") {
    assert(SqlType("int(1)").varSizeParseOpt === Some(("int", 1)))
    assert(SqlType("varchar(2)").varSizeParseOpt === Some(("varchar", 2)))
    assert(SqlType("foo(3)").varSizeParseOpt === Some(("foo", 3)))
    assert(SqlType("text").varSizeParseOpt.isEmpty)
    assert(SqlType("foo").varSizeParseOpt.isEmpty)
    assert(SqlType("foo()").varSizeParseOpt.isEmpty)
    assert(SqlType("foo(bar)").varSizeParseOpt.isEmpty)
    assert(SqlType("foo(0)").varSizeParseOpt.isEmpty)
    assert(SqlType("foo(-1)").varSizeParseOpt.isEmpty)
  }

}
