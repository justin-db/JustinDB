package justin.db

import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import justin.db.actors.{StorageNodeActor, StorageNodeActorRef}
import justin.db.client.ActorRefStorageNodeClient
import justin.db.consistenthashing.{NodeId, Ring}
import justin.db.replica.N

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class StorageNodeActorSpecMultiJvmNode1 extends FormTheClusterSpec
class StorageNodeActorSpecMultiJvmNode2 extends FormTheClusterSpec
class StorageNodeActorSpecMultiJvmNode3 extends FormTheClusterSpec
class StorageNodeActorSpecMultiJvmNode4 extends FormTheClusterSpec
class StorageNodeActorSpecMultiJvmNode5 extends FormTheClusterSpec

class FormTheClusterSpec extends MultiNodeSpec(StorageNodeActorConfig("FormTheClusterSpec"))
  with ScalaTestMultiNodeSpec
  with ImplicitSender {

  private val config = StorageNodeActorConfig("FormTheClusterSpec")
  import config._

  override def initialParticipants: Int = roles.size

  "Storage Node Actors" must {

    "wait for all nodes to enter a barrier" in {
      enterBarrier("startup")
    }

    "form the cluster" in within(10.seconds) {
      runOn(node1) {
        val _ = storageNodeActorRef(NodeId(0))
        enterBarrier("deployed")
      }
      runOn(node2) {
        val _ = storageNodeActorRef(NodeId(1))
        enterBarrier("deployed")
      }
      runOn(node3) {
        val _ = storageNodeActorRef(NodeId(2))
        enterBarrier("deployed")
      }
      runOn(node4) {
        val _ = storageNodeActorRef(NodeId(3))
        enterBarrier("deployed")
      }
      runOn(node5) {
        val _ = storageNodeActorRef(NodeId(4))
        enterBarrier("deployed")
      }

      enterBarrier("cluster-up")
    }
  }

  private def storageNodeActorRef(nodeId: NodeId) = new ActorRefStorageNodeClient(StorageNodeActorRef {
    system.actorOf(
      props = StorageNodeActor.props(nodeId, null, Ring.apply(nodesSize = nodes.size, partitionsSize =  nodes.size * 15), N(3)), // TODO: storage can't be null
      name  = StorageNodeActor.name(nodeId)
    )
  })
}
