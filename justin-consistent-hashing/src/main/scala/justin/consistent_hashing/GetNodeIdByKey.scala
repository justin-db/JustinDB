package justin.consistent_hashing

import java.util.UUID
import justin.consistent_hashing.NodeMapRing.RingKey

object UUID2RingKey extends (UUID => NodeMapRing.RingKey) {
  override def apply(id: UUID): RingKey = scala.math.abs(id.hashCode())
}

class GetNodeIdByKey(ring: NodeMapRing) extends (UUID => Option[NodeId]) {
  override def apply(id: UUID): Option[NodeId] = {
    ring.getByKey(UUID2RingKey(id) % ring.size)
  }
}
