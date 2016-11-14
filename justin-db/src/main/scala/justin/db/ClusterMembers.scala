package justin.db

import justin.db.consistent_hashing.NodeId

case class ClusterMembers(private val members: Map[NodeId, StorageNodeActorRef]) {

  def contains(nodeId: NodeId): Boolean = members.contains(nodeId)
  def notContains(nodeId: NodeId) = !contains(nodeId)

  def add(nodeId: NodeId, ref: StorageNodeActorRef): ClusterMembers = {
    ClusterMembers(this.members + (nodeId -> ref))
  }

  def get(nodeId: NodeId): Option[StorageNodeActorRef] = members.get(nodeId)
}

object ClusterMembers {
  def empty: ClusterMembers = ClusterMembers(members = Map.empty[NodeId, StorageNodeActorRef])
}
