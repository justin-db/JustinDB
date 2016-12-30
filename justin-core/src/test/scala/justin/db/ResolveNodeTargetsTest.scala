package justin.db

import akka.actor.ActorRef
import justin.consistent_hashing.NodeId
import org.scalatest.{FlatSpec, Matchers}

class ResolveNodeTargetsTest extends FlatSpec with Matchers {

  behavior of "Resolver of Node targets"

  it should "mark \"local\" value as \"true\" when node is placed in the preference list" in {
    // given
    val nodeId         = NodeId(1)
    val preferenceList = List(nodeId)
    val clusterMembers = ClusterMembers.empty

    // when
    val resolved = ResolveNodeTargets(nodeId, preferenceList, clusterMembers)

    // then
    resolved.local shouldBe true
  }

  it should "mark \"local\" value as \"false\" when node is placed in the preference list" in {
    // given
    val nodeId         = NodeId(1)
    val preferenceList = List.empty[NodeId]
    val clusterMembers = ClusterMembers.empty

    // when
    val resolved = ResolveNodeTargets(nodeId, preferenceList, clusterMembers)

    // then
    resolved.local shouldBe false
  }

  it should "not include node in remotes" in {
    // given
    val nodeId         = NodeId(1)
    val preferenceList = List(NodeId(1), NodeId(2), NodeId(3))
    val clusterMembers = ClusterMembers(Map(NodeId(2) -> StorageNodeActorRef(ActorRef.noSender), NodeId(3) -> StorageNodeActorRef(ActorRef.noSender)))

    // when
    val resolved = ResolveNodeTargets(nodeId, preferenceList, clusterMembers)

    // then
    resolved shouldBe ResolvedTargets(local = true, remotes = List(StorageNodeActorRef(null), StorageNodeActorRef(null)))
  }

  it should "distinct repeated elements in preference list" in {
    // given
    val nodeId         = NodeId(1)
    val preferenceList = List(NodeId(1), NodeId(2), NodeId(2))
    val clusterMembers = ClusterMembers(Map(NodeId(2) -> StorageNodeActorRef(ActorRef.noSender), NodeId(3) -> StorageNodeActorRef(ActorRef.noSender)))

    // when
    val resolved = ResolveNodeTargets(nodeId, preferenceList, clusterMembers)

    // then
    resolved shouldBe ResolvedTargets(local = true, remotes = List(StorageNodeActorRef(ActorRef.noSender)))
  }

  it should "flatten not existed nodes from preference list in cluster members" in {
    // given
    val nodeId         = NodeId(1)
    val preferenceList = List(NodeId(1), NodeId(2), NodeId(3))
    val clusterMembers = ClusterMembers(Map(NodeId(2) -> StorageNodeActorRef(ActorRef.noSender)))

    // when
    val resolved = ResolveNodeTargets(nodeId, preferenceList, clusterMembers)

    // then
    resolved shouldBe ResolvedTargets(local = true, remotes = List(StorageNodeActorRef(ActorRef.noSender)))
  }
}
