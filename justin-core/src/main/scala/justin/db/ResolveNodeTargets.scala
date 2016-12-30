package justin.db

import justin.consistent_hashing.NodeId

case class ResolvedTargets(local: Boolean, remotes: List[StorageNodeActorRef])

object ResolveNodeTargets {

  def apply(nodeId: NodeId, preferenceList: List[NodeId], clusterMembers: ClusterMembers): ResolvedTargets = {
    ResolvedTargets(
      local   = preferenceList.contains(nodeId),
      remotes = preferenceList.filterNot(_ == nodeId).distinct.flatMap(clusterMembers.get)
    )
  }
}
