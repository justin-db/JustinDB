package justin.consistent_hashing

import java.util.UUID

import org.scalatest.{FlatSpec, Matchers}

class GetNodeIdByUUIDTest extends FlatSpec with Matchers {

  behavior of "Get NodeId By UUID"

  it should "assign UUID to correct NodeId" in {
    val ring = Ring.apply(N = 3, S = 30)
    val getByKey = new GetNodeIdByUUID(ring, uuid => 30)

    getByKey(UUID.randomUUID()) shouldBe Some(NodeId(0))
  }
}
