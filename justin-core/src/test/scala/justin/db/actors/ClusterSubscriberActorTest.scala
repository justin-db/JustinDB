package justin.db.actors

import akka.actor.{Actor, ActorSystem}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import com.typesafe.config.ConfigFactory
import justin.consistent_hashing.{NodeId, Ring}
import justin.db.StorageNodeActorProtocol.RegisterNode
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

class ClusterSubscriberActorTest extends TestKit(ClusterSubscriberActorTest.actorSystem)
  with FlatSpecLike
  with ImplicitSender
  with Matchers
  with ScalaFutures
  with BeforeAndAfterAll {

  behavior of "Cluster Subscriber Actor trait"

  it should "send msg back with targeted NodeId when registration of other node has correctly happened" in {
    // given
    val nodeId = NodeId(1)
    val testActor = TestActorRef(new TestActor(nodeId, Ring.apply(3, 1)))

    // when
    testActor ! RegisterNode(NodeId(2))

    // then
    expectMsg(RegisterNode(nodeId))
  }

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  class TestActor(nodeId: NodeId, ring: Ring) extends Actor with ClusterSubscriberActor {
    override def receive: Receive = receiveClusterDataPF(nodeId, ring)
  }
}

object ClusterSubscriberActorTest {
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
