package justin.db.cluster

import justin.db.actors.StorageNodeActorRef
import justin.db.consistenthashing.NodeId

case class ClusterMembers(private val members: Map[NodeId, StorageNodeActorRef]) {

  def contains(nodeId: NodeId): Boolean = members.contains(nodeId)
  def notContains(nodeId: NodeId): Boolean = !contains(nodeId)

  def add(nodeId: NodeId, ref: StorageNodeActorRef): ClusterMembers = {
    println("add nodeId: " + nodeId + ", from ref: " + ref.ref)
    val a = ClusterMembers(this.members + (nodeId -> ref))
    println("--> members: " + a.toString)
    a
  }

  def get(nodeId: NodeId): Option[StorageNodeActorRef] = members.get(nodeId)

  def removeByRef(ref: StorageNodeActorRef): ClusterMembers = {
    val filteredMembers = members.filterNot { case (_, sRef) => sRef == ref }
    ClusterMembers(filteredMembers)
  }

  def size: Int = members.size

  override def toString: String = members.toString()
}

object ClusterMembers {
  def empty: ClusterMembers = ClusterMembers(Map.empty[NodeId, StorageNodeActorRef])
}
