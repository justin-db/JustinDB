package justin.consistent_hashing

import java.util.UUID
import justin.consistent_hashing.NodeMapRing.RingPartitionId

object UUID2PartitionId extends (UUID => NodeMapRing.RingPartitionId) {
  override def apply(id: UUID): RingPartitionId = scala.math.abs(id.hashCode())
}

class GetNodeIdByPartitionId(ring: NodeMapRing, uUID2RingKey: UUID => RingPartitionId = UUID2PartitionId) extends (UUID => Option[NodeId]) {
  override def apply(id: UUID): Option[NodeId] = {
    ring.getByPartitionId(uUID2RingKey(id) % ring.size)
  }
}
