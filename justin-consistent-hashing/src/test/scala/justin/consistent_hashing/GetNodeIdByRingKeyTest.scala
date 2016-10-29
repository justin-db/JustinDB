package justin.consistent_hashing

import java.util.UUID

import org.scalatest.{FlatSpec, Matchers}

class GetNodeIdByRingKeyTest extends FlatSpec with Matchers {

  behavior of "Get NodeId By RingKey"

  it should "assign Key to correct NodeId" in {
    val ring = NodeMapRing.apply(N = 3, S = 30)
    val getByKey = new GetNodeIdByRingKey(ring, uuid => 30)

    getByKey(UUID.randomUUID()) shouldBe Some(NodeId(0))
  }
}
