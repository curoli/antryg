package antryg.util

import org.scalatest.FunSuite

class CombinatorTest extends FunSuite {

  test("combinations") {
    val input = Seq(Set(1), Set(2, 3), Set(1,2,3))
    val combinations = Combinator.combinations(input)
    val combinationsExpected = Set(Seq(1, 2, 1), Seq(1, 2, 2), Seq(1, 2, 3), Seq(1, 3, 1), Seq(1, 3, 2), Seq(1, 3, 3))
    assert(combinations === combinationsExpected)
  }

}
