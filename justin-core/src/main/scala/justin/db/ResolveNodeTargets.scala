package justin.db

import justin.consistent_hashing.NodeId
import justin.db.replication.PreferenceList

case class ResolvedTargets(local: Boolean, remotes: List[StorageNodeActorRef])

object ResolveNodeTargets {

  def apply(nodeId: NodeId, preferenceList: PreferenceList, clusterMembers: ClusterMembers): ResolvedTargets = {
    ResolvedTargets(
      local   = preferenceList.list.contains(nodeId),
      remotes = preferenceList.list.filterNot(_ == nodeId).distinct.flatMap(clusterMembers.get)
    )
  }
}
