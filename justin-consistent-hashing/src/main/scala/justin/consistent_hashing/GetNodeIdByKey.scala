package justin.consistent_hashing

import java.util.UUID

import justin.consistent_hashing.NodeMapRing.RingKey

object UUID2RingKey extends (UUID => NodeMapRing.RingKey) {
  override def apply(id: UUID): RingKey = id.hashCode()
}

class GetNodeIdByKey(ring: NodeMapRing) extends (UUID => Option[NodeId]) {
  override def apply(id: UUID): Option[NodeId] = {
    val key = UUID2RingKey(id) % ring.size
    ring.getByKey(key)
  }
}
