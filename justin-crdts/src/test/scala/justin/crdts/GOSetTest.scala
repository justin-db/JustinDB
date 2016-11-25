package justin.crdts

import org.scalatest.{FlatSpec, Matchers}

class GOSetTest extends FlatSpec with Matchers {

  behavior of "Grow-only set (CRDT)"

  it should "define contains method for checking element in set" in {
    // given
    val goset = GOSet(Set(1,2,3))
    val elem  = 2

    // when
    val exists = goset.contains(elem)

    // then
    exists shouldBe true
  }

  it should "create set with added element when updating base one" in {
    // given
    val baseSet = GOSet(Set(1,2,3))
    val addElem  = AddElement(4)

    // when
    val updated = baseSet.update(addElem)

    // then
    updated shouldBe GOSet(Set(1,2,3,4))
  }
}
