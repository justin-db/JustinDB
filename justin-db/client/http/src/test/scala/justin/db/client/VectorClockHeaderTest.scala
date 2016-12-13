package justin.db.client

import justin.consistent_hashing.NodeId
import justin.vector_clocks.{Counter, VectorClock}
import org.scalatest.{FlatSpec, Matchers}

class VectorClockHeaderTest extends FlatSpec with Matchers {

  behavior of "Vector Clock Header"

  it should "parse string and create Vector Clock instance upon it" in {
    // given
    val encoded = "W1siMSIsMV0sWyIyIiwyXSxbIjMiLDldXQ=="

    // when
    val vClockHeader = VectorClockHeader.parse(encoded).get

    // then
    vClockHeader.vectorClock shouldBe VectorClock(Map(NodeId(1) -> Counter(1), NodeId(2) -> Counter(2), NodeId(3) -> Counter(9)))
  }

  it should "stringify Vector Clock instance" in {
    // given
    val vClock = VectorClock(Map(NodeId(1) -> Counter(1), NodeId(2) -> Counter(2), NodeId(3) -> Counter(9)))

    // when
    val encoded = VectorClockHeader(vClock).value()

    // then
    encoded shouldBe "W1siMSIsMV0sWyIyIiwyXSxbIjMiLDldXQ=="
  }
}
