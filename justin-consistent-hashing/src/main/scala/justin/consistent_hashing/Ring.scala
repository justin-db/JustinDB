package justin.consistent_hashing

case class Ring(private val ring: Map[Ring.RingPartitionId, NodeId]) {
  import Ring.RingPartitionId

  def getByPartitionId(id: RingPartitionId): Option[NodeId] = ring.get(id)

  lazy val size: Int = ring.size

  lazy val nodesId: Set[NodeId] = ring.values.toSet

  lazy val swap: Map[NodeId, List[RingPartitionId]] = {
    ring.groupBy { case (_, nodeId) => nodeId }
      .mapValues(_.keys.toList.sorted)
  }
}

object Ring {
  type RingPartitionId = Int

  def apply(N: Int = 3, S: Int = 50): Ring = {
    val ring = for {
      id      <- 0 until N
      ringKey <- id until S by N
    } yield (ringKey, NodeId(id))

    Ring(ring.toMap)
  }
}
