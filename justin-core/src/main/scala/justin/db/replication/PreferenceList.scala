package justin.db.replication

import justin.consistent_hashing.Ring.RingPartitionId
import justin.consistent_hashing.{NodeId, Ring}

object PreferenceList {

  sealed trait Error
  case object LackOfCoordinator extends Error
  case class NotSufficientSize(preferenceList: PreferenceList) extends Error

  def apply(baseRingPartitionId: RingPartitionId, n: N, ring: Ring): Either[Error, PreferenceList] = {
    ring.getNodeId(baseRingPartitionId) match {
      case None => Left(LackOfCoordinator)
      case Some(coordinatorNodeId) =>
        val maxPartitionId = baseRingPartitionId + n.n - 1
        val restNodesIds   = (baseRingPartitionId until maxPartitionId).map(getNextNodeId(ring)).flatten.distinct.filterNot(_ == coordinatorNodeId)
        val preferenceList = PreferenceList(coordinatorNodeId, restNodesIds.toList)

        preferenceList.size >= n.n match {
          case true  => Right(preferenceList)
          case false => Left(NotSufficientSize(preferenceList))
        }
    }
  }

  private def getNextNodeId(ring: Ring) = ring.nextPartitionId _ andThen ring.getNodeId _
}

case class PreferenceList(primaryNodeId: NodeId, replicasNodeId: List[NodeId]) {
  def size: Int = 1 + replicasNodeId.size // 1 means Primary
}
