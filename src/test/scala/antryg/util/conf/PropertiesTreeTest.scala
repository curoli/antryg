package antryg.util.conf

import org.scalatest.FunSuite

class PropertiesTreeTest extends FunSuite {

  test("PropertiesTree.{splitKeyPath, valueOpt, children, +, ++, get, subTree}") {
    import PropertiesTree.splitKeyPath
    assert(splitKeyPath("") === Seq.empty)
    assert(splitKeyPath("a") === Seq("a"))
    assert(splitKeyPath("a.b") === Seq("a", "b"))
    assert(splitKeyPath("a.b.c") === Seq("a", "b", "c"))
    val tree =
      PropertiesTree.empty + ("a.b.c", "foo") + ("a.b", "bar") + ("a.b.c", "baz") + ("", "yo") + ("x.y.z", "hello") ++
        Map("q.w.e.t" -> "blub", "q.w.r.t" -> "foo", "q" -> "hi")
    val aTree = PropertiesTree.empty + ("b", "bar") + ("b.c", "baz")
    assert(tree.valueOpt === Some("yo"))
    assert(aTree.valueOpt === None)
    assert(tree.children.get("a") === Some(aTree))
    assert(tree.get("") === Some("yo"))
    assert(tree.get("a") === None)
    assert(tree.get("a.b") === Some("bar"))
    assert(tree.get("a.b.c") === Some("baz"))
    assert(tree.get("x.y.z.z") === None)
    assert(tree.get("q.w.e.t") === Some("blub"))
    assert(tree.subTree("a") === aTree)
  }

}
