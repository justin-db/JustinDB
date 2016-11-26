package justin.db

import akka.remote.testkit.{MultiNodeConfig, MultiNodeSpec}
import akka.testkit.ImplicitSender

object MultiStorageNodeActorConfig extends MultiNodeConfig {
  val node1 = role("node1")
  val node2 = role("node2")
}

class MultiStorageNodeActorMultiJvmNode1 extends MultiStorageNodeActorSpec
class MultiStorageNodeActorMultiJvmNode2 extends MultiStorageNodeActorSpec

class MultiStorageNodeActorSpec extends MultiNodeSpec(MultiStorageNodeActorConfig)
  with STMultiNodeSpec
  with ImplicitSender {

  override def initialParticipants: Int = roles.size
}
