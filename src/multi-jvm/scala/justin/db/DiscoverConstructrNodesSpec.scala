package justin.db

import java.util.Base64

import akka.actor.{Actor, Address, AddressFromURIString, Props}
import akka.cluster.{ClusterEvent, MemberStatus}
import akka.pattern.ask
import akka.remote.testkit.{MultiNodeConfig, MultiNodeSpec}
import akka.testkit.TestDuration
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import de.heikoseeberger.constructr.ConstructrExtension
import io.circe.Json
import io.circe.parser.parse

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

final class DiscoverMultiNodeConfig extends MultiNodeConfig {
  val first  = role("first")
  val second = role("second")
  val third  = role("third")
  val fourth = role("fourth")
  val fifth  = role("fifth")

  commonConfig(MultiNodeClusterSpec.clusterConfig)

  for ((roleName, idx) <- List(first, second, third, fourth, fifth).zipWithIndex) {
    val port = 2551 + idx
    nodeConfig(roleName)(
      ConfigFactory.parseString(s"""
        akka.remote.netty.tcp.hostname = "127.0.0.1"
        akka.remote.netty.tcp.port     = $port
        constructr.coordination.host   = "127.0.0.1"
        constructr.coordination.port   = 2379
        """
      )
    )
  }

  def toNodes(s: String): Set[Address] = {
    def jsonToNode(json: Json) = {
      val key =
        json.hcursor
          .get[String]("key")
          .fold(throw _, identity)
          .stripPrefix("/constructr/DiscoverConstructrNodesSpec/nodes/")
      AddressFromURIString(new String(Base64.getUrlDecoder.decode(key)))
    } // for Scala 2.11
    parse(s)
      .fold(throw _, identity)
      .hcursor
      .downField("node")
      .get[Set[Json]]("nodes")
      .getOrElse(Set.empty)
      .map(jsonToNode)
  }
}

final class MultiNodeEtcdConstructrSpecMultiJvmNode1 extends DiscoverConstructrNodesSpec
final class MultiNodeEtcdConstructrSpecMultiJvmNode2 extends DiscoverConstructrNodesSpec
final class MultiNodeEtcdConstructrSpecMultiJvmNode3 extends DiscoverConstructrNodesSpec
final class MultiNodeEtcdConstructrSpecMultiJvmNode4 extends DiscoverConstructrNodesSpec
final class MultiNodeEtcdConstructrSpecMultiJvmNode5 extends DiscoverConstructrNodesSpec

abstract class DiscoverConstructrNodesSpec(config: DiscoverMultiNodeConfig)
  extends MultiNodeSpec(config)
  with MultiNodeClusterSpec {

  def this() = this(new DiscoverMultiNodeConfig())

  "Constructr should manage an Akka cluster" in {
    enterBarrier("coordination-started")

    ConstructrExtension(system)

    val listener = system.actorOf(Props(new Actor {
      import ClusterEvent._

      var isMember = false
      cluster.subscribe(self, InitialStateAsEvents, classOf[MemberJoined], classOf[MemberUp])

      override def receive: Receive = {
        case "isMember" => sender() ! isMember
        case MemberJoined(member) if member.address == cluster.selfAddress => isMember = true
        case MemberUp(member) if member.address == cluster.selfAddress => isMember = true
      }
    }))

    within(20.seconds.dilated) {
      awaitAssert {
        implicit val timeout = Timeout(1.second.dilated)
        val isMember         = Await.result((listener ? "isMember").mapTo[Boolean], 1.second.dilated)
        isMember shouldBe true
      }
    }

    enterBarrier("cluster-formed")

    within(10.seconds.dilated) {
      awaitAssert {
        cluster.state.members.size shouldBe initialParticipants
        cluster.state.members.forall(_.status == MemberStatus.Up) shouldBe true
      }
    }

    enterBarrier("nodes-up")
  }

  override def initialParticipants: Int = roles.size
}
