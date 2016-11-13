package justin.db.consistent_hashing

import java.util.UUID
import justin.db.consistent_hashing.Ring.RingPartitionId

class UUID2RingPartitionId(ring: Ring) extends (UUID => Ring.RingPartitionId) {
  override def apply(id: UUID): RingPartitionId = scala.math.abs(id.hashCode()) % ring.size
}
