package justin.db

import akka.actor.ActorRef
import justin.consistent_hashing.NodeId
import justin.db.actors.StorageNodeActorRef
import org.scalatest.{FlatSpec, Matchers}

class ClusterMembersTest extends FlatSpec with Matchers {

  behavior of "Cluster Members"

  it should "define an \"empty\" method which create an empty ClusterMember with size 0" in {
    // given
    val expectedSize = 0

    // when
    val emtpyClusterMembers = ClusterMembers.empty

    // then
    emtpyClusterMembers.size shouldBe expectedSize
  }

  it should "define immutable \"add\" method for adding pair of NodeId with ActorRef" in {
    // given
    val emptyClusterMembers = ClusterMembers.empty
    val nodeId = NodeId(100)
    val ref = StorageNodeActorRef(ActorRef.noSender)

    // when
    val updatedClusterMembers = emptyClusterMembers.add(nodeId, ref)

    // then
    updatedClusterMembers shouldBe ClusterMembers(Map(nodeId -> ref))
  }

  it should "give false result when asking for non-existent element with \"contains\" method" in {
    // given
    val emptyClusterMembers = ClusterMembers.empty

    // when
    val exists = emptyClusterMembers.contains(NodeId(1))

    // then
    exists shouldBe false
  }

  it should "give positive result when asking for existent element with \"contains\" method" in {
    // given
    val nodeId = NodeId(100)
    val ref = StorageNodeActorRef(ActorRef.noSender)
    val clusterMembers = ClusterMembers.empty.add(nodeId, ref)

    // when
    val exists = clusterMembers.contains(NodeId(100))

    // then
    exists shouldBe true
  }

  it should "give positive result when asking for non-existent element with \"notContains\" method" in {
    // given
    val emptyClusterMembers = ClusterMembers.empty

    // when
    val notExists = emptyClusterMembers.notContains(NodeId(1))

    // then
    notExists shouldBe true
  }

  it should "return an element by key using \"get\" method" in {
    val nodeId = NodeId(100)
    val ref = StorageNodeActorRef(ActorRef.noSender)
    val clusterMembers = ClusterMembers.empty.add(nodeId, ref)

    clusterMembers.get(nodeId)     shouldBe defined
    clusterMembers.get(NodeId(99)) should not be defined
  }

  it should "remove an element by its value" in {
    val nodeId = NodeId(100)
    val ref = StorageNodeActorRef(ActorRef.noSender)
    val clusterMembers = ClusterMembers.empty.add(nodeId, ref)

    clusterMembers.removeByRef(ref) shouldBe ClusterMembers.empty
  }

  it should "stringify" in {
    // given
    val nodeId = NodeId(100)
    val ref = StorageNodeActorRef(ActorRef.noSender)
    val emptyClusterMembers = ClusterMembers(Map(nodeId -> ref))

    // when
    val stringified = emptyClusterMembers.toString

    // then
    stringified shouldBe Map(nodeId -> ref).toString()
  }
}
