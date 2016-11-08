package justin.consistent_hashing

class Ring(private val ring: Map[Ring.RingPartitionId, NodeId]) {
  import Ring.RingPartitionId

  def getNodeId(id: RingPartitionId): Option[NodeId] = ring.get(id)

  lazy val size: Int = ring.size

  lazy val nodesId: Set[NodeId] = ring.values.toSet

  lazy val swap: Map[NodeId, List[RingPartitionId]] = {
    ring.groupBy { case (_, nodeId) => nodeId }
      .mapValues(_.keys.toList.sorted)
  }
}

object Ring {
  type RingPartitionId = Int

  /**
    *
    * @param N - nr of initial cluster size of nodes
    * @param S - nr of partitions Ring consists of
    * @return representation of Ring
    */
  def apply(N: Int = 5, S: Int = 64): Ring = {
    val partitions2Nodes = for {
      id          <- 0 until N
      partitionId <- id until S by N
    } yield (partitionId, NodeId(id))

    new Ring(partitions2Nodes.toMap)
  }
}
