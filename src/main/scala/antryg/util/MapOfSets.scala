package antryg.util

object MapOfSets {

  def fromMaps[K, V](maps: Iterable[Map[K, V]]): Map[K, Set[V]] = {
    val keys = maps.map(_.keySet).fold(Set.empty)(_ ++ _)
    keys.map(key => (key, maps.flatMap(_.get(key)).toSet)).toMap
  }

  def filterForKey[K, V](map: Map[K, Set[V]], key: K, filter: V => Boolean): Map[K, Set[V]] = {
    val setFiltered = map.getOrElse(key, Set.empty).filter(filter)
    map + (key -> setFiltered)
  }

  def unions[K, V](map1: Map[K, Set[V]], map2: Map[K, Set[V]]): Map[K, Set[V]] = {
    val keys = map1.keySet ++ map2.keySet
    keys.map(key => (key, map1.getOrElse(key, Set.empty) ++ map2.getOrElse(key, Set.empty))).toMap
  }

  def intersections[K, V](map1: Map[K, Set[V]], map2: Map[K, Set[V]]): Map[K, Set[V]] = {
    val keys = map1.keySet.intersect(map2.keySet)
    keys.map(key => (key, map1(key).intersect(map2(key)))).filter(_._2.nonEmpty).toMap
  }

}
