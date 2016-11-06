package justin.consistent_hashing

import java.util.UUID

import org.scalatest.{FlatSpec, Matchers}

class GetNodeIdByPartitionIdTest extends FlatSpec with Matchers {

  behavior of "Get NodeId By PartitionId"

  it should "assign PartitionId to correct NodeId" in {
    val ring = Ring.apply(N = 3, S = 30)
    val getByKey = new GetNodeIdByPartitionId(ring, uuid => 30)

    getByKey(UUID.randomUUID()) shouldBe Some(NodeId(0))
  }
}
