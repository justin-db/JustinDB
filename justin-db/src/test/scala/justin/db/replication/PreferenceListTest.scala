package justin.db.replication

import justin.db.consistent_hashing.Ring
import org.scalatest.{FlatSpec, Matchers}

class PreferenceListTest extends FlatSpec with Matchers {

  behavior of "Preference List"

  it should "has size of defined nr of replicas" in {
    val n = N(3) // nr of replicas
    val ring = Ring(nodesSize = 5, partitionsSize = 64)
    val basePartitionId = 1

    val expectedSize = 3

    PreferenceList(basePartitionId, n, ring).size shouldBe expectedSize
  }

  it should "has defined first node in the list to be the one taken from Ring with initial partitionId" in {
    val n = N(3) // nr of replicas
    val ring = Ring(nodesSize = 5, partitionsSize = 64)
    val initialPartitionId = 1

    val coordinator = PreferenceList.apply(initialPartitionId, n, ring).head

    coordinator shouldBe ring.getNodeId(initialPartitionId).get
  }
}
