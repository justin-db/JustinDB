package justin.db

import justin.consistent_hashing.NodeId
import justin.db.actors.StorageNodeActorRef

case class ClusterMembers(private val members: Map[NodeId, StorageNodeActorRef]) {

  def contains(nodeId: NodeId): Boolean = members.contains(nodeId)
  def notContains(nodeId: NodeId): Boolean = !contains(nodeId)

  def add(nodeId: NodeId, ref: StorageNodeActorRef): ClusterMembers = {
    ClusterMembers(this.members + (nodeId -> ref))
  }

  def get(nodeId: NodeId): Option[StorageNodeActorRef] = members.get(nodeId)

  def size: Int = members.size
}

object ClusterMembers {
  def empty: ClusterMembers = ClusterMembers(members = Map.empty[NodeId, StorageNodeActorRef])
}
