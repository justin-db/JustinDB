package justin.db

import java.util.UUID

import akka.actor.ActorRef
import akka.pattern.ask
import akka.remote.testkit.{MultiNodeConfig, MultiNodeSpec}
import akka.util.Timeout
import justin.db.actors.StorageNodeActor
import justin.db.consistenthashing.{NodeId, Ring}
import justin.db.replica.N
import justin.db.storage.{JustinData, PluggableStorageProtocol}
import akka.testkit.TestDuration
import justin.db.cluster.ClusterMembers

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

final class ConvergeJustinDBClusterConfig extends MultiNodeConfig {
  val first  = role("first")
  val second = role("second")
  val third  = role("third")
  val fourth = role("fourth")
  val fifth  = role("fifth")

  commonConfig(MultiNodeClusterSpec.clusterConfig)
}

final class ConvergeJustinDBClusterSpecMultiJvmNode1 extends ConvergeJustinDBClusterSpec
final class ConvergeJustinDBClusterSpecMultiJvmNode2 extends ConvergeJustinDBClusterSpec
final class ConvergeJustinDBClusterSpecMultiJvmNode3 extends ConvergeJustinDBClusterSpec
final class ConvergeJustinDBClusterSpecMultiJvmNode4 extends ConvergeJustinDBClusterSpec
final class ConvergeJustinDBClusterSpecMultiJvmNode5 extends ConvergeJustinDBClusterSpec

abstract class ConvergeJustinDBClusterSpec(config: ConvergeJustinDBClusterConfig)
  extends MultiNodeSpec(config)
  with MultiNodeClusterSpec {

  import config._

  def this() = this(new ConvergeJustinDBClusterConfig())

  "A cluster of 5 members" must {

    "setup stable akka cluster" in {
      awaitConstructRClusterUp()
      enterBarrier("akka-cluster-up")
    }

    "reach final convergence" in within(50.seconds) {
      runOn(first) {
        val ref = storageNodeActor(0)
        enterBarrier("storage-node-actor-up")

        awaitMembers(ref)
        enterBarrier("cluster-converged")
      }
      runOn(second) {
        val ref = storageNodeActor(1)
        enterBarrier("storage-node-actor-up")

        awaitMembers(ref)
        enterBarrier("cluster-converged")
      }
      runOn(third) {
        val ref = storageNodeActor(2)
        enterBarrier("storage-node-actor-up")

        awaitMembers(ref)
        enterBarrier("cluster-converged")
      }
      runOn(fourth) {
        val ref = storageNodeActor(3)
        enterBarrier("storage-node-actor-up")

        awaitMembers(ref)
        enterBarrier("cluster-converged")
      }
      runOn(fifth) {
        val ref = storageNodeActor(4)
        enterBarrier("storage-node-actor-up")

        awaitMembers(ref)
        enterBarrier("cluster-converged")
      }
    }
  }

  private[this] def awaitMembers(ref: ActorRef) = awaitAssert(
    a = {
      implicit val timeout = Timeout(1.second.dilated)
      val members = Await.result((ref ? "members").mapTo[ClusterMembers], 1.second.dilated)
      members.size shouldBe initialParticipants
    },
    interval = 2.seconds
  )

  private[this] def storageNodeActor(nodeId: Int) = {
    val storage = new PluggableStorageProtocol {
      override def get(id: UUID)(resolveOriginality: (UUID) => PluggableStorageProtocol.DataOriginality): Future[PluggableStorageProtocol.StorageGetData] = ???
      override def put(data: JustinData)(resolveOriginality: (UUID) => PluggableStorageProtocol.DataOriginality): Future[PluggableStorageProtocol.Ack] = ???
    }
    val ring = Ring(nodesSize = 5, partitionsSize = 100)
    val n = N(3)
    val nodeid = NodeId(nodeId)

    system.actorOf(
      props = StorageNodeActor.props(nodeid, storage, ring, n),
      name  = StorageNodeActor.name(nodeid)
    )
  }
}
