package justin.db

import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender

import scala.concurrent.duration._

class StorageNodeActorSpecMultiJvmNode1 extends StorageNodeActorSpec
class StorageNodeActorSpecMultiJvmNode2 extends StorageNodeActorSpec
class StorageNodeActorSpecMultiJvmNode3 extends StorageNodeActorSpec

class StorageNodeActorSpec extends MultiNodeSpec(StorageNodeActorConfig)
  with ScalaTestMultiNodeSpec
  with ImplicitSender {

  import StorageNodeActorConfig._

  override def initialParticipants: Int = roles.size

  private[this] val seedStorageNodeAddress = node(seed).address
  private[this] val storageNode1Address    = node(node1).address
  private[this] val storageNode2Address    = node(node2).address

  muteDeadLetters(classOf[Any])(system)

  "Storage Node Actors" must {

    "form the cluster" in within(10.seconds) {

      Cluster(system).subscribe(testActor, classOf[MemberUp])
      expectMsgClass(classOf[CurrentClusterState])

      Cluster(system).join(seedStorageNodeAddress)

      receiveN(3).map { case MemberUp(m) => m.address }.toSet shouldBe Set(seedStorageNodeAddress, storageNode1Address, storageNode2Address)

      Cluster(system).unsubscribe(testActor)

      enterBarrier("cluster-up")
    }
  }
}
