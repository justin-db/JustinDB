package justin.consistent_hashing

import java.util.UUID
import justin.consistent_hashing.Ring.RingPartitionId

object UUID2PartitionId extends (UUID => Ring.RingPartitionId) {
  override def apply(id: UUID): RingPartitionId = scala.math.abs(id.hashCode())
}

class GetNodeIdByUUID(ring: Ring, UUID2PartitionId: UUID => RingPartitionId) extends (UUID => Option[NodeId]) {
  override def apply(id: UUID): Option[NodeId] = {
    ring.getNodeId(UUID2PartitionId(id) % ring.size)
  }
}
