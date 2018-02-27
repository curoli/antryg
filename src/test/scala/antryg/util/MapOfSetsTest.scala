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

  test("filterForKey") {
    val map = Map(1 -> Set(1, 2, 3), 2 -> Set(4, 5, 6, 7, 8), 3 -> Set(10, 20, 30))
    val filteredMap = MapOfSets.filterForKey(map, 2, (num: Int) => num % 2 == 0)
    val expectedFilteredMap = Map(1 -> Set(1, 2, 3), 2 -> Set(4, 6, 8), 3 -> Set(10, 20, 30))
    assert(filteredMap === expectedFilteredMap)
  }

  test("unions") {
    val map1 = Map(1 -> Set(1, 2, 3), 2 -> Set(4), 3 -> Set(4, 5, 6))
    val map2 = Map(1 -> Set(4, 5, 6), 2 -> Set(4), 7 -> Set(10, 20, 30))
    val mapOfUnions = MapOfSets.unions(map1, map2)
    val expectedMapOfUnions = Map(1 -> Set(1, 2, 3, 4, 5, 6), 2 -> Set(4), 3 -> Set(4, 5, 6), 7 -> Set(10, 20, 30))
    assert(mapOfUnions === expectedMapOfUnions)
  }

  test("intersections") {
    val map1 = Map(1 -> Set(1, 2, 3), 2 -> Set(4, 5), 3 -> Set(4, 5, 6))
    val map2 = Map(1 -> Set(4, 5, 6), 2 -> Set(5, 6), 3 -> Set(3, 4), 7 -> Set(10, 20, 30))
    val mapOfUnions = MapOfSets.intersections(map1, map2)
    val expectedMapOfUnions = Map(2 -> Set(5), 3 -> Set(4))
    assert(mapOfUnions === expectedMapOfUnions)
  }

}
