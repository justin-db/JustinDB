package justin.db

import org.scalatest.{FlatSpec, Matchers}

class StorageNodeActorTest extends FlatSpec with Matchers {

  behavior of "Storage Node Actor"

  it should "has defined role \"StorageNode\"" in {
    StorageNodeActor.role shouldBe "StorageNode"
  }

  it should "define name for actor with combination of its id" in {
    StorageNodeActor.name(StorageNodeId(10)) shouldBe "id-10"
  }
}
