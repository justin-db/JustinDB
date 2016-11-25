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
}
