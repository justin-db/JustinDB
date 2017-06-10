package justin.httpapi

import justin.db.consistenthashing.NodeId
import justin.db.vectorclocks.{Counter, VectorClock}
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

  it should "throw an Exception for not parsable Vector Clock" in {
    val vClock = null

    intercept[VectorClockHeaderException] {
      val encoded = VectorClockHeader(vClock).value()
    }
  }

  it should "render header in response" in {
    VectorClockHeader(null).renderInResponses() shouldBe true
  }

  it should "render header in request" in {
    VectorClockHeader(null).renderInRequests() shouldBe true
  }
}
