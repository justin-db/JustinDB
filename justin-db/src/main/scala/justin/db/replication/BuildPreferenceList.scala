package justin.db.replication

import justin.db.StorageNodeActorId

// TODO: preference list should take into account how Ring actually is build (current version is rather naive)
object BuildPreferenceList {
  def apply(baseId: StorageNodeActorId, n: N): List[StorageNodeActorId] = {
    val floor   = baseId.id + 1
    val ceiling = baseId.id + n.n

    (floor to ceiling)
      .filterNot(_ == baseId.id)
      .map(StorageNodeActorId)
      .toList
  }
}
