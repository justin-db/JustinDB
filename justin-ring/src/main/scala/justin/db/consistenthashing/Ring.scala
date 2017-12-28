package justin.db.consistenthashing

import justin.db.consistenthashing.Ring.RingPartitionId

class Ring(val ring: Map[RingPartitionId, NodeId]) {

  lazy val size: Int = ring.size

  lazy val nodesId: Set[NodeId] = ring.values.toSet

  lazy val swap: Map[NodeId, List[RingPartitionId]] = {
    ring.groupBy(_._2).mapValues(_.keys.toList.sorted)
  }

  def getNodeId(id: RingPartitionId): Option[NodeId] = ring.get(id)

  def updated(ringPartitionId: RingPartitionId, nodeId: NodeId): Ring = new Ring(ring.updated(ringPartitionId, nodeId))

  def nextPartitionId(id: RingPartitionId): RingPartitionId = (id + 1) % ring.size

  override def toString: String = ring.toString()
}

object Ring {
  type RingPartitionId = Int

  sealed trait AddNodeResult
  case object AlreadyExistingNodeId extends AddNodeResult
  case class UpdatedRingWithTakenPartitions(ring: Ring, takeOverDataFrom: List[(RingPartitionId, NodeId)]) extends AddNodeResult

  def addNode(ring: Ring, nodeId: NodeId): Ring.AddNodeResult = {
    if(ring.nodesId.contains(nodeId)) {
      Ring.AlreadyExistingNodeId
    } else {
      // this could be improved e.g. we should rely on least taken resources
      val takeOverDataFrom = (0 until ring.size by (ring.nodesId.size + 1))
        .flatMap { ringPartitionId => ring.getNodeId(ringPartitionId).map(nodeId => (ringPartitionId, nodeId)) }
        .toList

      val updatedRing = takeOverDataFrom.foldLeft(ring) {
        case (acc, (ringPartitionId, _)) => acc.updated(ringPartitionId, nodeId)
      }

      Ring.UpdatedRingWithTakenPartitions(updatedRing, takeOverDataFrom)
    }
  }

  def apply(nodesSize: Int, partitionsSize: Int): Ring = {
    val partitions2Nodes = for {
      id          <- 0 until nodesSize
      partitionId <- id until partitionsSize by nodesSize
    } yield (partitionId, NodeId(id))

    new Ring(partitions2Nodes.toMap)
  }
}
