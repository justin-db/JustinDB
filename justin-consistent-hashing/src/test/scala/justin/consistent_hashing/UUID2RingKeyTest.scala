package justin.consistent_hashing

import java.util.UUID

import org.scalatest.{FlatSpec, Matchers}

class UUID2RingKeyTest extends FlatSpec with Matchers {

  behavior of "UUID to RingKey mapper"

  it should "use inner hashCode of UUID to map to RingKey" in {
    val uid = UUID.randomUUID()
    val uidHashCode = uid.hashCode()

    UUID2RingKey(uid) shouldBe uidHashCode
  }
}
