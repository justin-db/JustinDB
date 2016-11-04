package justin.consistent_hashing

import java.util.UUID

import org.scalatest.{FlatSpec, Matchers}

class UUID2PartitionIdTest extends FlatSpec with Matchers {

  behavior of "UUID to PartitionId function"

  it should "use inner hashCode with scala.math.abs on it" in {
    val uid = UUID.randomUUID()
    val uidHashCode = uid.hashCode()

    UUID2PartitionId(uid) shouldBe scala.math.abs(uidHashCode)
  }
}
