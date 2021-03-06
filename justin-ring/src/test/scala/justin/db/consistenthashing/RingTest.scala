package justin.db.consistenthashing

import org.scalatest.{FlatSpec, Matchers}

class RingTest extends FlatSpec with Matchers {

  behavior of "Ring"

  it should "define its size by nr of partitions" in {
    val N = 3  // nr of nodes
    val S = 50 // nr of partitions

    val ring = Ring.apply(N, S)

    ring.size shouldBe S
  }

  it should "has expected set of node ids" in {
    val N = 3  // nr of nodes
    val S = 50 // nr of partitions

    val ring = Ring.apply(N, S)

    ring.nodesId shouldBe Set(NodeId(0), NodeId(1), NodeId(2))
  }

  it should "initialize Ring with vnodes" in {
    val N = 5  // nr of nodes - recommended by Riak database team
    val S = 64 // nr of partitions - recommended by Riak database team

    val ring = Ring.apply(N, S)

    val expectedSwappedRing = Map(
      NodeId(0) -> List(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60),
      NodeId(1) -> List(1, 6, 11, 16, 21, 26, 31, 36, 41, 46, 51, 56, 61),
      NodeId(2) -> List(2, 7, 12, 17, 22, 27, 32, 37, 42, 47, 52, 57, 62),
      NodeId(3) -> List(3, 8, 13, 18, 23, 28, 33, 38, 43, 48, 53, 58, 63),
      NodeId(4) -> List(4, 9, 14, 19, 24, 29, 34, 39, 44, 49, 54, 59)
    )

    ring.swap shouldBe expectedSwappedRing
  }

  it should "start indexing of partitions from 0" in {
    val ring = Ring.apply(nodesSize = 5, partitionsSize = 64)

    ring.getNodeId(0) shouldBe defined
  }

  it should "end indexing of partitions with index that is minus one of ring's size" in {
    val ring = Ring.apply(nodesSize = 5, partitionsSize = 64)

    val lastIdx = ring.size - 1

    ring.getNodeId(lastIdx)     shouldBe defined
    ring.getNodeId(lastIdx + 1) should not be defined
  }

  it should "stringify itself" in {
    val ring = Ring.apply(nodesSize = 2, partitionsSize = 2)

    ring.toString shouldBe "Map(0 -> NodeId(0), 1 -> NodeId(1))"
  }

  it should "update value for particular key" in {
    val ring = Ring.apply(nodesSize = 5, partitionsSize = 64)

    val updatedRing = ring.updated(ringPartitionId = 2, nodeId = NodeId(100))

    updatedRing.getNodeId(id = 2) shouldBe Some(NodeId(100))
  }

  behavior of "Ring Add Node"

  it should "end up with AlreadyExistingNodeId when trying to add reserved node id" in {
    val ring = Ring.apply(nodesSize = 5, partitionsSize = 64)

    Ring.addNode(ring, nodeId = NodeId(0)) shouldBe Ring.AlreadyExistingNodeId
    Ring.addNode(ring, nodeId = NodeId(1)) shouldBe Ring.AlreadyExistingNodeId
    Ring.addNode(ring, nodeId = NodeId(2)) shouldBe Ring.AlreadyExistingNodeId
    Ring.addNode(ring, nodeId = NodeId(3)) shouldBe Ring.AlreadyExistingNodeId
    Ring.addNode(ring, nodeId = NodeId(4)) shouldBe Ring.AlreadyExistingNodeId
  }

  it should "take over some partitions by added node" in {
    // given
    val nodesSize      = 4
    val partitionsSize = 36
    val ring           = Ring.apply(nodesSize, partitionsSize)

    // when
    val nodeId          = NodeId(5)
    val updateResult    = Ring.addNode(ring, nodeId).asInstanceOf[Ring.UpdatedRingWithTakenPartitions]
    val updatedRing     = updateResult.ring
    val takenPartitions = updateResult.takeOverDataFrom

    // then
    updatedRing.ring.size shouldBe ring.size
    updatedRing.nodesId   shouldBe (ring.nodesId + nodeId)

    takenPartitions should not be empty
  }
}
