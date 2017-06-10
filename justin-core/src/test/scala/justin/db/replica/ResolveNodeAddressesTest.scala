package justin.db.replica

import akka.actor.ActorRef
import justin.db.actors.StorageNodeActorRef
import justin.db.cluster.ClusterMembers
import justin.db.consistenthashing.NodeId
import org.scalatest.{FlatSpec, Matchers}

class ResolveNodeAddressesTest extends FlatSpec with Matchers {

  behavior of "Resolver of Node Addresses"

  it should "mark \"local\" value as \"true\" when node is placed in the preference list" in {
    // given
    val nodeId         = NodeId(1)
    val preferenceList = PreferenceList(nodeId, Nil)
    val clusterMembers = ClusterMembers.empty

    // when
    val resolved = ResolveNodeAddresses(nodeId, preferenceList, clusterMembers)

    // then
    resolved.local shouldBe true
  }

  it should "mark \"local\" value as \"false\" when node is NOT placed in the preference list" in {
    // given
    val nodeId         = NodeId(1)
    val preferenceList = PreferenceList(NodeId(2), Nil)
    val clusterMembers = ClusterMembers.empty

    // when
    val resolved = ResolveNodeAddresses(nodeId, preferenceList, clusterMembers)

    // then
    resolved.local shouldBe false
  }

  it should "not include node in remotes" in {
    // given
    val nodeId         = NodeId(1)
    val preferenceList = PreferenceList(NodeId(1), List(NodeId(2), NodeId(3)))
    val clusterMembers = ClusterMembers(Map(NodeId(2) -> StorageNodeActorRef(ActorRef.noSender), NodeId(3) -> StorageNodeActorRef(ActorRef.noSender)))

    // when
    val resolved = ResolveNodeAddresses(nodeId, preferenceList, clusterMembers)

    // then
    resolved shouldBe ResolvedNodeAddresses(local = true, remotes = List(StorageNodeActorRef(ActorRef.noSender), StorageNodeActorRef(ActorRef.noSender)))
  }

  it should "flatten not existed nodes from preference list in cluster members" in {
    // given
    val nodeId         = NodeId(1)
    val preferenceList = PreferenceList(NodeId(1), List(NodeId(2), NodeId(3)))
    val clusterMembers = ClusterMembers(Map(NodeId(2) -> StorageNodeActorRef(ActorRef.noSender)))

    // when
    val resolved = ResolveNodeAddresses(nodeId, preferenceList, clusterMembers)

    // then
    resolved shouldBe ResolvedNodeAddresses(local = true, remotes = List(StorageNodeActorRef(ActorRef.noSender)))
  }
}
