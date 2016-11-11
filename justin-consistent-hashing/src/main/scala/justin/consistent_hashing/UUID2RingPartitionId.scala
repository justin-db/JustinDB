package justin.consistent_hashing

import java.util.UUID
import justin.consistent_hashing.Ring.RingPartitionId

class UUID2RingPartitionId(ringSize: RingSize) extends (UUID => Ring.RingPartitionId) {
  override def apply(id: UUID): RingPartitionId = scala.math.abs(id.hashCode()) % ringSize.size
}
