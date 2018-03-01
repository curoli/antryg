package antryg.util

object Combinator {

  def combinations[V](seqs: Seq[Set[V]]): Set[Seq[V]] = {
    if(seqs.isEmpty) {
      Set.empty
    } else if(seqs.size == 1) {
      seqs.head.map(Seq(_))
    } else {
      val head = seqs.head
      val tailCombinations = combinations(seqs.tail)
      head.flatMap(headElem => tailCombinations.map(tailCombination => headElem +: tailCombination))
    }
  }

}
