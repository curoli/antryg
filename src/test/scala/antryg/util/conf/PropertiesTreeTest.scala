package antryg.util.conf

import org.scalatest.FunSuite

class PropertiesTreeTest extends FunSuite {

  test("PropertiesTree.{valueOpt, children, +, ++, get, splitKeyPath}") {
    import PropertiesTree.splitKeyPath
    assert(splitKeyPath("") === Seq.empty)
    assert(splitKeyPath("a") === Seq("a"))
    assert(splitKeyPath("a.b") === Seq("a", "b"))
    assert(splitKeyPath("a.b.c") === Seq("a", "b", "c"))
    val tree =
      PropertiesTree.empty + ("a.b.c", "foo") + ("a.b", "bar") + ("a.b.c", "baz") + ("", "yo") + ("x.y.z", "hello") ++
        Map("q.w.e.t" -> "blub", "q.w.r.t" -> "foo", "q" -> "hi")
    assert(tree.get("") === Some("yo"))
    assert(tree.get("a") === None)
    assert(tree.get("a.b") === Some("bar"))
    assert(tree.get("a.b.c") === Some("baz"))
    assert(tree.valueOpt === Some("yo"))
    assert(tree.get("x.y.z.z") === None)
    assert(tree.get("q.w.e.t") === Some("blub"))
    val aTree = PropertiesTree.empty + ("b", "bar") + ("b.c", "baz")
    assert(aTree.valueOpt === None)
    assert(tree.children.get("a") === Some(aTree))
  }

}
