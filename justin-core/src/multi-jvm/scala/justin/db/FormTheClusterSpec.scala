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

class FormTheClusterSpec extends MultiNodeSpec(StorageNodeActorConfig("StorageNodeActorSpec"))
  with ScalaTestMultiNodeSpec
  with ImplicitSender {

  private val config = StorageNodeActorConfig("StorageNodeActorSpec")
  import config._

  override def initialParticipants: Int = roles.size

  private[this] val seedStorageNodeAddress = node(seed).address
  private[this] val storageNode1Address    = node(node1).address
  private[this] val storageNode2Address    = node(node2).address

  muteDeadLetters(classOf[Any])(system)

  "Storage Node Actors" must {

    "wait for all nodes to enter a barrier" in {
      enterBarrier("startup")
    }

    "form the cluster" in within(10.seconds) {
      runOn(seed) {
        val storageNodeActor1 = storageNodeActorRef(NodeId(0))
        enterBarrier("deployed")
      }
      runOn(node1) {
        val storageNodeActor2 = storageNodeActorRef(NodeId(1))
        enterBarrier("deployed")

      }
      runOn(node2) {
        val storageNodeActor3 = storageNodeActorRef(NodeId(2))
        enterBarrier("deployed")
      }

      enterBarrier("cluster-up")
    }
  }

  private def storageNodeActorRef(nodeId: NodeId) = new ActorRefStorageNodeClient(StorageNodeActorRef {
    system.actorOf(
      props = StorageNodeActor.props(nodeId, null, Ring.apply(nodesSize = 3, partitionsSize =  21), N(3)),
      name  = StorageNodeActor.name(nodeId)
    )
  })
}
