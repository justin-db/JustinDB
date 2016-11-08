package justin.consistent_hashing

import java.util.UUID
import justin.consistent_hashing.Ring.RingPartitionId

class UUID2PartitionId(ringSize: RingSize) extends (UUID => Ring.RingPartitionId) {
  override def apply(id: UUID): RingPartitionId = scala.math.abs(id.hashCode()) % ringSize.size
}

class GetNodeIdByUUID(ring: Ring, UUID2PartitionId: UUID => RingPartitionId) extends (UUID => Option[NodeId]) {
  override def apply(id: UUID): Option[NodeId] = ring.getNodeId(UUID2PartitionId(id))
}
