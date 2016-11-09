package justin.db.replication

import justin.consistent_hashing.RingSize
import org.scalatest.{FlatSpec, Matchers}

class PreferenceListTest extends FlatSpec with Matchers {

  behavior of "Preference List"

  it should "has size of defined nr of replicas" in {
    val n = N(3) // nr of replicas
    val ringSize = RingSize(64)
    val basePartitionId = 1

    val expectedSize = 3

    PreferenceList.apply(basePartitionId, n, ringSize).size shouldBe expectedSize
  }

  it should "has defined head of preference-list as a \"basePartitionId\" (coordinator)" in {
    val n = N(3) // nr of replicas
    val ringSize = RingSize(64)
    val basePartitionId = 1

    val coordinator = PreferenceList.apply(basePartitionId, n, ringSize).head

    coordinator shouldBe basePartitionId
  }

  it should "ring when partitions finished" in {
    val n = N(5) // nr of replicas
    val ringSize = RingSize(64)
    val basePartitionId = 62

    val list = PreferenceList.apply(basePartitionId, n, ringSize)

    list shouldBe List(62, 63, 0, 1, 2)
  }
}
