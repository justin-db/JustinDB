package justin.db.replica

import justin.consistent_hashing.NodeId
import justin.db.actors.StorageNodeActorRef
import justin.db.cluster.ClusterMembers

case class ResolvedNodeAddresses(local: Boolean, remotes: List[StorageNodeActorRef])

object ResolveNodeAddresses {

  def apply(nodeId: NodeId, preferenceList: PreferenceList, clusterMembers: ClusterMembers): ResolvedNodeAddresses = {
    ResolvedNodeAddresses(
      local   = preferenceList.all.contains(nodeId),
      remotes = preferenceList.all.flatMap(clusterMembers.get)
    )
  }
}
