package justin.db

import akka.actor.ActorRef
import justin.db.consistent_hashing.NodeId
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

}
