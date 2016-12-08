package justin.db.versioning

import org.scalatest.{FlatSpec, Matchers}
import justin.vector_clocks.{Counter, VectorClock}

class VectorClockOpsTest extends FlatSpec with Matchers {

  behavior of "Vector Clock Ops"

  it should "create Vector Clock instance from plain string" in {
    "A:2".toVectorClock[String]           shouldBe VectorClock(Map("A" -> Counter(2)))
    "A:1, B:2".toVectorClock[String]      shouldBe VectorClock(Map("A" -> Counter(1), "B" -> Counter(2)))
    "A:1, B:1, C:1".toVectorClock[String] shouldBe VectorClock(Map("A" -> Counter(1), "B" -> Counter(1), "C" -> Counter(1)))
  }
}
