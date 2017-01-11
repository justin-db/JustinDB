package justin.db

import java.util.UUID

import justin.consistent_hashing.{NodeId, Ring, UUID2RingPartitionId}
import justin.db.storage.PluggableStorageProtocol.DataOriginality

/**
  * Created by new on 11.01.2017.
  */
class ResolveDataOriginality(nodeId: NodeId, ring: Ring) extends (UUID => DataOriginality) {
  override def apply(id: UUID): DataOriginality = {
    val partitionId = UUID2RingPartitionId(id, ring)
    ring.getNodeId(partitionId).contains(nodeId) match {
      case true  => DataOriginality.Primary(partitionId)
      case false => DataOriginality.Replica(partitionId)
    }
  }
}
