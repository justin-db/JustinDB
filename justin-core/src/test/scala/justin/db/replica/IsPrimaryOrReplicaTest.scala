package justin.db.replica

import java.util.UUID

import justin.db.consistenthashing.{NodeId, Ring}
import justin.db.storage.PluggableStorageProtocol.DataOriginality
import org.scalatest.{FlatSpec, Matchers}

class IsPrimaryOrReplicaTest extends FlatSpec with Matchers {

  behavior of "Data Originality Resolver"

  it should "reason exemplary data's id as a replica" in {
    // given
    val nodeId   = NodeId(0)
    val ring     = Ring.apply(nodesSize = 3, partitionsSize = 21)
    val resolver = new IsPrimaryOrReplica(nodeId, ring)
    val id       = UUID.fromString("179d6eb0-681d-4277-9caf-3d6d60e9faf9")

    // when
    val originality = resolver.apply(id)

    // then
    originality shouldBe a[DataOriginality.Replica]
  }

  it should "reason exemplary data's id as a primary" in {
    // given
    val nodeId   = NodeId(0)
    val ring     = Ring.apply(nodesSize = 3, partitionsSize = 21)
    val resolver = new IsPrimaryOrReplica(nodeId, ring)
    val id       = UUID.fromString("16ec44cd-5b4e-4b38-a647-206c1dc11b50")

    // when
    val originality = resolver.apply(id)

    // then
    originality shouldBe a[DataOriginality.Primary]
  }
}
