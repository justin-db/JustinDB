package justin.db

import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.remote.testconductor.RoleName
import akka.remote.testkit.{MultiNodeConfig, MultiNodeSpec}
import akka.testkit.ImplicitSender
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

object MultiStorageNodeActorConfig extends MultiNodeConfig {
  val seed: RoleName  = role("StorageNode-seed")
  val node1: RoleName = role("StorageNode-1")
  val node2: RoleName = role("StorageNode-2")

  commonConfig(ConfigFactory.parseString("""
    akka.actor.provider="akka.cluster.ClusterActorRefProvider"
    # don't use sigar for tests, native lib not in path
    akka.cluster.metrics.collector-class = akka.cluster.JmxMetricsCollector"""
  ))
}

class MultiStorageNodeActorSpecMultiJvmNode1 extends MultiStorageNodeActorSpec
class MultiStorageNodeActorSpecMultiJvmNode2 extends MultiStorageNodeActorSpec
class MultiStorageNodeActorSpecMultiJvmNode3 extends MultiStorageNodeActorSpec

class MultiStorageNodeActorSpec extends MultiNodeSpec(MultiStorageNodeActorConfig)
  with STMultiNodeSpec
  with ImplicitSender {

  import MultiStorageNodeActorConfig._

  override def initialParticipants: Int = roles.size

  val seedStorageNodeAddress = node(seed).address
  val storageNode1Address    = node(node1).address
  val storageNode2Address    = node(node2).address

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
