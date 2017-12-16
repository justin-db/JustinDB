package justin.db.actors

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import com.typesafe.config.ConfigFactory
import justin.db.actors.protocol.RegisterNode
import justin.db.cluster.datacenter.Datacenter
import justin.db.consistenthashing.{NodeId, Ring}
import justin.db.replica.N
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

class StorageNodeActorTest extends TestKit(StorageNodeActorTest.actorSystem)
  with FlatSpecLike
  with ImplicitSender
  with Matchers
  with ScalaFutures
  with BeforeAndAfterAll {

  behavior of "Storage Node Actor"

  it should "send msg back with targeted NodeId when registration of other node has correctly happened" in {
    // given
    val nodeId = NodeId(1)
    val testActor = TestActorRef(new TestActor(nodeId, Ring.apply(3, 1)))

    // when
    testActor ! RegisterNode(NodeId(2))

    // then
    expectMsg(RegisterNode(nodeId))
  }

  it should "has defined role \"storagenode\"" in {
    StorageNodeActor.role shouldBe "storagenode"
  }

  it should "compose its name based on datacenter it belongs to and given id" in {
    StorageNodeActor.name(NodeId(0), Datacenter("dc1"))   shouldBe "dc1-id-0"
    StorageNodeActor.name(NodeId(10), Datacenter("dc2"))  shouldBe "dc2-id-10"
    StorageNodeActor.name(NodeId(20), Datacenter("dc1"))  shouldBe "dc1-id-20"
    StorageNodeActor.name(NodeId(999), Datacenter("dc1")) shouldBe "dc1-id-999"
  }

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  class TestActor(nodeId: NodeId, ring: Ring) extends StorageNodeActor(nodeId, Datacenter("default"), null, ring, N(1))
}

object StorageNodeActorTest {
  def actorSystem: ActorSystem = {
    val config = ConfigFactory.parseString(
      """
        |akka.loglevel = off
        |akka.actor.provider = cluster
        |akka.cluster.auto-join = off
        |akka.cluster.metrics.enabled = off
      """.stripMargin).withFallback(ConfigFactory.load())

    ActorSystem("test-system", config)
  }
}
