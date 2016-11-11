package justin.db.replication

import justin.db.consistent_hashing.Ring.RingPartitionId
import justin.db.consistent_hashing.RingSize

object PreferenceList {
  def apply(baseId: RingPartitionId, n: N, ringSize: RingSize): List[RingPartitionId] = {
    (0 until n.n)
      .map(idx => (baseId + idx) % ringSize.size)
      .toList
  }
}
