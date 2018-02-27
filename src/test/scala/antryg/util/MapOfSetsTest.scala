package antryg.util

import org.scalatest.FunSuite

class MapOfSetsTest extends FunSuite {

  test("fromMaps") {
    val map1 = Map(1 -> 2, 2 -> 3, 3 -> 4)
    val map2 = Map(1 -> 2, 2 -> 5, 6 -> 7)
    val map3 = Map(1 -> 2, 2 -> 5, 7 -> 8)
    val maps = Seq(map1, map2, map3)
    val mapOfSets = MapOfSets.fromMaps(maps)
    val expectedMapOfSets = Map(1 -> Set(2), 2 -> Set(3, 5), 3 -> Set(4), 6 -> Set(7), 7 -> Set(8))
    assert(mapOfSets === expectedMapOfSets)
  }

}
