package justin.db.replication

import justin.db.consistent_hashing.Ring
import org.scalatest.{FlatSpec, Matchers}

class PreferenceListTest extends FlatSpec with Matchers {

  behavior of "Preference List"

  it should "has size of defined nr of replicas" in {
    // given
    val n = N(3) // nr of replicas
    val ring = Ring(nodesSize = 5, partitionsSize = 64)
    val basePartitionId = 1

    // when
    val preferenceList = PreferenceList(basePartitionId, n, ring)

    // then
    preferenceList.size shouldBe 3
  }

  it should "has defined first node in the list to be the one taken from Ring with initial partitionId" in {
    // given
    val n = N(3) // nr of replicas
    val ring = Ring(nodesSize = 5, partitionsSize = 64)
    val initialPartitionId = 1

    // when
    val coordinator = PreferenceList.apply(initialPartitionId, n, ring).head

    // then
    coordinator shouldBe ring.getNodeId(initialPartitionId).get
  }

  it should "has at least one member (coordinator) for none replicas" in {
    // given
    val n = N(0) // nr of replicas
    val ring = Ring(nodesSize = 5, partitionsSize = 64)
    val initialPartitionId = 1

    // when
    val preferenceList = PreferenceList.apply(initialPartitionId, n, ring)

    // then
    preferenceList.size shouldBe 1
    preferenceList.head shouldBe ring.getNodeId(initialPartitionId).get
  }
}
