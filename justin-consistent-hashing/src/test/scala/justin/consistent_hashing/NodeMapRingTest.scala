package justin.consistent_hashing

import org.scalatest.{FlatSpec, Matchers}

class NodeMapRingTest extends FlatSpec with Matchers {

  behavior of "Node Map Ring"

  it should "be initialized with declaration of nr of Nodes and nr of ring slices per node" in {
    val N = 3  // nr of nodes
    val S = 50 // nr of slices

    val ring = NodeMapRing.apply(N, S)

    ring.size         shouldBe S
    ring.nodesId.size shouldBe N
  }

  it should "has expected set of node ids" in {
    val N = 3  // nr of nodes
    val S = 50 // nr of slices

    val ring = NodeMapRing.apply(N, S)

    ring.nodesId shouldBe Set(NodeId(0), NodeId(1), NodeId(2))
  }
}
