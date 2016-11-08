package justin.db.replication

import justin.consistent_hashing.Ring.RingPartitionId
import justin.consistent_hashing.RingSize

object BuildPreferenceList {
  def apply(baseId: RingPartitionId, n: N, ringSize: RingSize): List[RingPartitionId] = {
    (0 until n.n)
      .map(idx => (baseId + idx) % ringSize.size)
      .toList
  }
}
