package antryg.util

object MapOfSets {

  def fromMaps[K, V](maps: Iterable[Map[K, V]]): Map[K, Set[V]] = {
    val keys = maps.map(_.keySet).fold(Set.empty)(_ ++ _)
    keys.map(key => (key, maps.flatMap(_.get(key)).toSet)).toMap
  }

}
