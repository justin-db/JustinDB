package justin.db

import akka.actor.{Actor, RootActorPath}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.cluster.{Cluster, Member, MemberStatus}
import justin.consistent_hashing.{NodeId, Ring}
import justin.db.StorageNodeActorProtocol.RegisterNode

trait ClusterSubscriberActor { self: Actor =>

  private val cluster = Cluster(context.system)

  var clusterMembers = ClusterMembers.empty

  override def preStart(): Unit = cluster.subscribe(this.self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(this.self)

  def receiveClusterPF(nodeId: NodeId, ring: Ring): Receive = {
    case RegisterNode(senderNodeId) if clusterMembers.notContains(senderNodeId) =>
      clusterMembers = clusterMembers.add(senderNodeId, StorageNodeActorRef(sender()))
      sender() ! RegisterNode(nodeId)
    case MemberUp(m)                => register(nodeId, ring, m)
    case state: CurrentClusterState => state.members.filter(_.status == MemberStatus.Up).foreach(m => register(nodeId, ring, m))
  }

  private def register(nodeId: NodeId, ring: Ring, member: Member) = {
    val nodesRefs = for {
      siblingNodeId <- ring.nodesId.filterNot(_ == nodeId)
      nodeName       = StorageNodeActor.name(siblingNodeId)
      nodeRef        = context.actorSelection(RootActorPath(member.address) / "user" / nodeName)
    } yield nodeRef

    nodesRefs.foreach(_ ! RegisterNode(nodeId))
  }
}
