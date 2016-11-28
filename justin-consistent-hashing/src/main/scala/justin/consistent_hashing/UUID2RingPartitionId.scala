package justin.consistent_hashing

import java.util.UUID

import justin.consistent_hashing.Ring.RingPartitionId

object UUID2RingPartitionId extends ((UUID, Ring) => Ring.RingPartitionId) {
  override def apply(id: UUID, ring: Ring): RingPartitionId = scala.math.abs(id.hashCode()) % ring.size
}
