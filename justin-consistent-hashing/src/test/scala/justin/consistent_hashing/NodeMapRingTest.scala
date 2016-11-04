package justin.consistent_hashing

import org.scalatest.{FlatSpec, Matchers}

class NodeMapRingTest extends FlatSpec with Matchers {

  behavior of "Node Map Ring"

  it should "be initialized with declaration of nr of Nodes and nr of ring partitions per node" in {
    val N = 3  // nr of nodes
    val S = 50 // nr of partitions

    val ring = NodeMapRing.apply(N, S)

    ring.size         shouldBe S
    ring.nodesId.size shouldBe N
  }

  it should "has expected set of node ids" in {
    val N = 3  // nr of nodes
    val S = 50 // nr of partitions

    val ring = NodeMapRing.apply(N, S)

    ring.nodesId shouldBe Set(NodeId(0), NodeId(1), NodeId(2))
  }

  it should "initialize Ring with vnodes" in {
    val N = 5  // nr of nodes - recommended by Riak database team
    val S = 64 // nr of partitions - recommended by Riak database team

    val ring = NodeMapRing.apply(N, S)

    val expectedSwappedRing = Map(
      NodeId(0) -> List(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60),
      NodeId(1) -> List(1, 6, 11, 16, 21, 26, 31, 36, 41, 46, 51, 56, 61),
      NodeId(2) -> List(2, 7, 12, 17, 22, 27, 32, 37, 42, 47, 52, 57, 62),
      NodeId(3) -> List(3, 8, 13, 18, 23, 28, 33, 38, 43, 48, 53, 58, 63),
      NodeId(4) -> List(4, 9, 14, 19, 24, 29, 34, 39, 44, 49, 54, 59)
    )

    ring.swap shouldBe expectedSwappedRing
  }
}
