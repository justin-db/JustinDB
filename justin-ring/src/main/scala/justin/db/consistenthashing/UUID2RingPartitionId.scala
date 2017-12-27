package justin.db.consistenthashing

import java.util.UUID

import justin.db.consistenthashing.Ring.RingPartitionId

object UUID2RingPartitionId extends ((UUID, Ring) => Ring.RingPartitionId) {
  override def apply(id: UUID, ring: Ring): RingPartitionId = scala.math.abs(id.hashCode()) % ring.size
}
