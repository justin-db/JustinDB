package justin.db.actors

import justin.consistent_hashing.NodeId
import org.scalatest.{FlatSpec, Matchers}

class StorageNodeActorTest extends FlatSpec with Matchers {

  behavior of "Storage Node Actor"

  it should "has defined role \"storagenode\"" in {
    StorageNodeActor.role shouldBe "storagenode"
  }

  it should "define name for actor with combination of its id" in {
    StorageNodeActor.name(NodeId(0))   shouldBe "id-0"
    StorageNodeActor.name(NodeId(10))  shouldBe "id-10"
    StorageNodeActor.name(NodeId(20))  shouldBe "id-20"
    StorageNodeActor.name(NodeId(999)) shouldBe "id-999"
  }
}
