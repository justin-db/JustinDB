package justin.db

// TODO: preference list should take into account how Ring actually is build (current version is rather naive)
object BuildPreferenceList {
  def apply(baseId: StorageNodeActorId, replicationFactor: ReplicationFactor): List[StorageNodeActorId] = {
    val floor   = baseId.id + 1
    val ceiling = baseId.id + replicationFactor.n

    (floor to ceiling)
      .filterNot(_ == baseId.id)
      .map(StorageNodeActorId)
      .toList
  }
}

case class ReplicationFactor(n: Int) extends AnyVal
