package justin.consistent_hashing

import java.util.UUID
import justin.consistent_hashing.Ring.RingPartitionId

object UUID2PartitionId extends (UUID => Ring.RingPartitionId) {
  override def apply(id: UUID): RingPartitionId = scala.math.abs(id.hashCode())
}

class GetNodeIdByPartitionId(ring: Ring, uUID2RingKey: UUID => RingPartitionId = UUID2PartitionId) extends (UUID => Option[NodeId]) {
  override def apply(id: UUID): Option[NodeId] = {
    ring.getNodeId(uUID2RingKey(id) % ring.size)
  }
}
