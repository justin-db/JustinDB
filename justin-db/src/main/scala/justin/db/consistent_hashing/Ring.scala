package justin.db.consistent_hashing

import Ring.RingPartitionId

class Ring(private val ring: Map[RingPartitionId, NodeId]) {

  def getNodeId(id: RingPartitionId): Option[NodeId] = ring.get(id)

  lazy val size = ring.size

  lazy val nodesId: Set[NodeId] = ring.values.toSet

  lazy val swap: Map[NodeId, List[RingPartitionId]] = {
    ring.groupBy(_._2).mapValues(_.keys.toList.sorted)
  }

  def nextPartitionId(id: RingPartitionId): RingPartitionId = (id + 1) % ring.size

  override def toString: String = ring.toString()
}

object Ring {
  type RingPartitionId = Int

  /**
    *
    * @param nodesSize - nr of initial cluster size of nodes
    * @param partitionsSize - nr of partitions Ring consists of
    * @return representation of Ring
    */
  def apply(nodesSize: Int = 5, partitionsSize: Int = 64): Ring = {
    val partitions2Nodes = for {
      id          <- 0 until nodesSize
      partitionId <- id until partitionsSize by nodesSize
    } yield (partitionId, NodeId(id))

    new Ring(partitions2Nodes.toMap)
  }
}
