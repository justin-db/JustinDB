package justin.consistent_hashing

import java.util.UUID

import org.scalatest.{FlatSpec, Matchers}

class UUID2RingPartitionIdTest extends FlatSpec with Matchers {

  behavior of "mapping function from UUID to Ring's PartitionId"

  it should "use inner hashCode with scala.math.abs on it" in {
    val uid         = UUID.randomUUID()
    val uidHashCode = uid.hashCode()
    val ringSize    = RingSize(2)

    val expectedPartitionId = scala.math.abs(uidHashCode) % ringSize.size

    new UUID2RingPartitionId(ringSize).apply(uid) shouldBe expectedPartitionId
  }
}
