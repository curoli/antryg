package antryg

import org.scalatest.FunSuite

/**
  * antryg
  * Created by oruebenacker on 17.11.17.
  */
class PlaceHolderTest extends FunSuite {
  test("placeholder!") {
    assert(PlaceHolder.hello === "Hello, World!")
  }

}
