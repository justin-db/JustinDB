package justin.db.replication

import justin.consistent_hashing.Ring.RingPartitionId
import justin.consistent_hashing.{NodeId, Ring}

object PreferenceList {
  def apply(baseId: RingPartitionId, n: N, ring: Ring): List[NodeId] = {
    val coordinatorNodeId = ring.getNodeId(baseId)

    val maxPartitionId = baseId + n.n - 1
    val restNodesIds   = (baseId until maxPartitionId).toList.map(getNextNodeId(ring))

    (coordinatorNodeId :: restNodesIds).flatten
  }

  private def getNextNodeId(ring: Ring) = ring.nextPartitionId _ andThen ring.getNodeId _
}
