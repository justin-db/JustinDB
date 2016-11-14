package justin.db

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

}
