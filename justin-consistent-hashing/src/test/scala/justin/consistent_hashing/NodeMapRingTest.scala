package justin.consistent_hashing

import org.scalatest.{FlatSpec, Matchers}

class NodeMapRingTest extends FlatSpec with Matchers {

  behavior of "Node Map Ring"

  it should "be initialized with declaration of nr of Nodes and nr of ring slices per node" in {
    val N = 3  // nr of nodes
    val S = 50 // nr of slices

    NodeMapRing.apply(N, S)
  }

}
