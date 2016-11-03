package justin.db

import org.scalatest.{FlatSpec, Matchers}

class StorageNodeTest extends FlatSpec with Matchers {

  behavior of "Storage Node"

  it should "has defined role \"StorageNode\"" in {
    StorageNode.role shouldBe "StorageNode"
  }

  it should "define name for actor with combination of its id" in {
    StorageNode.name(StorageNodeId(10)) shouldBe "id-10"
  }
}
