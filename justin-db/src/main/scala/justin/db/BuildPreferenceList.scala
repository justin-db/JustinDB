package justin.db

// TODO: preference list should take into account how Ring actually is build (current version is rather naive)
object BuildPreferenceList {
  def apply(baseId: StorageNodeId, replicationFactor: ReplicationFactor): List[StorageNodeId] = {
    val floor   = baseId.id + 1
    val ceiling = baseId.id + replicationFactor.n

    (floor to ceiling)
      .filterNot(_ == baseId.id)
      .map(StorageNodeId)
      .toList
  }
}

case class ReplicationFactor(n: Int) extends AnyVal
