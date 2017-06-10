
package justin.db.versioning

import justin.db.consistenthashing.NodeId
import justin.vector_clocks.{Counter, VectorClock}
import org.scalatest.{FlatSpec, Matchers}

class VectorClockOpsTest extends FlatSpec with Matchers {

  behavior of "Vector Clock Ops"

  it should "create Vector Clock instance from plain string" in {
    "A:2".toVectorClock[String]           shouldBe VectorClock(Map("A" -> Counter(2)))
    "A:1, B:2".toVectorClock[String]      shouldBe VectorClock(Map("A" -> Counter(1), "B" -> Counter(2)))
    "A:1, B:1, C:1".toVectorClock[String] shouldBe VectorClock(Map("A" -> Counter(1), "B" -> Counter(1), "C" -> Counter(1)))
  }

  it should "create Vector Clock instance from plain string with numerical ids" in {
    import VectorClockOps.intAsId

    ("1:2": VectorClock[Int])       shouldBe VectorClock(Map(1 -> Counter(2)))
    ("1:2, 2:10": VectorClock[Int]) shouldBe VectorClock(Map(1 -> Counter(2), 2 -> Counter(10)))
  }

  it should "create Vector Clock instance from plain string with id being type of NodeId" in {
    import VectorClockOps.nodeIdAsId

    ("1:2": VectorClock[NodeId])       shouldBe VectorClock(Map(NodeId(1) -> Counter(2)))
    ("1:2, 2:10": VectorClock[NodeId]) shouldBe VectorClock(Map(NodeId(1) -> Counter(2), NodeId(2) -> Counter(10)))
  }
}
