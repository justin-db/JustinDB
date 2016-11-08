package justin.db.replication

import justin.consistent_hashing.RingSize
import justin.db.StorageNodeActorId

object BuildPreferenceList {
  def apply(baseId: StorageNodeActorId, n: N, ringSize: RingSize): List[StorageNodeActorId] = {
    (0 until n.n)
      .map(idx => StorageNodeActorId((baseId.id + idx) % ringSize.size))
      .toList
  }
}
