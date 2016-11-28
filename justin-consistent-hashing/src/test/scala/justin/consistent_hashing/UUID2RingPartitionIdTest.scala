package justin.consistent_hashing

import java.util.UUID

import org.scalatest.{FlatSpec, Matchers}

class UUID2RingPartitionIdTest extends FlatSpec with Matchers {

  behavior of "mapping function from UUID to Ring's PartitionId"

  it should "use inner hashCode with scala.math.abs on it" in {
    val uid         = UUID.randomUUID()
    val uidHashCode = uid.hashCode()
    val ring        = Ring(nodesSize = 1, partitionsSize = 2)

    val expectedPartitionId = scala.math.abs(uidHashCode) % ring.size

    UUID2RingPartitionId.apply(uid, ring) shouldBe expectedPartitionId
  }
}
