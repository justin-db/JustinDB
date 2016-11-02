package justin.db

import org.scalatest.{FlatSpec, Matchers}

class StorageNodeTest extends FlatSpec with Matchers {

  behavior of "Storage Node"

  it should "build preference list" in {
    val nodeId = StorageNodeId(1)
    val replicationFactor = ReplicationFactor(3)

    val list = StorageNode.buildPreferenceList(baseId = nodeId, replicationFactor = replicationFactor)

    list shouldBe List(StorageNodeId(2), StorageNodeId(3), StorageNodeId(4))
  }
}
