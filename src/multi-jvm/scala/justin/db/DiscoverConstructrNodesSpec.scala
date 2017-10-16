package justin.db

import java.util.Base64

import akka.actor.{Actor, Address, AddressFromURIString, Props}
import akka.cluster.{Cluster, ClusterEvent}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.StatusCodes.{NotFound, OK}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.pattern.ask
import akka.remote.testkit.{MultiNodeConfig, MultiNodeSpec}
import akka.stream.ActorMaterializer
import akka.testkit.TestDuration
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import de.heikoseeberger.constructr.ConstructrExtension
import io.circe.Json
import io.circe.parser.parse

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

final class DiscoverMultiNodeConfig extends MultiNodeConfig {

  val first = role("first")
  val second = role("second")
  val third = role("third")
  val fourth = role("fourth")
  val fifth = role("fifth")

  val coordinationPort: Int = 2379
  val delete = "/v2/keys/constructr?recursive=true"
  val get = "/v2/keys/constructr/DiscoverConstructrNodesSpec/nodes"

  commonConfig(MultiNodeClusterSpec.clusterConfig)

  for ((roleName, idx) <- List(first, second, third, fourth, fifth).zipWithIndex) {
    val port = 2551 + idx
    nodeConfig(roleName)(
      ConfigFactory.parseString(s"""
        akka.remote.netty.tcp.hostname = "127.0.0.1"
        akka.remote.netty.tcp.port     = $port
        constructr.coordination.host   = "127.0.0.1"
        constructr.coordination.port   = $coordinationPort
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

class MultiNodeEtcdConstructrSpecMultiJvmNode1 extends DiscoverConstructrNodesSpec
class MultiNodeEtcdConstructrSpecMultiJvmNode2 extends DiscoverConstructrNodesSpec
class MultiNodeEtcdConstructrSpecMultiJvmNode3 extends DiscoverConstructrNodesSpec
class MultiNodeEtcdConstructrSpecMultiJvmNode4 extends DiscoverConstructrNodesSpec
class MultiNodeEtcdConstructrSpecMultiJvmNode5 extends DiscoverConstructrNodesSpec

abstract class DiscoverConstructrNodesSpec(config: DiscoverMultiNodeConfig)
  extends MultiNodeSpec(config)
  with MultiNodeClusterSpec {
  import RequestBuilding._
  import system.dispatcher

  implicit val mat = ActorMaterializer()

  def this() = this(new DiscoverMultiNodeConfig())

  "Constructr should manage an Akka cluster" in {
    runOn(roles.head) {
      within(20.seconds.dilated) {
        awaitAssert {
          val coordinationStatus =
            Await.result(
              Http().singleRequest(Delete(s"http://127.0.0.1:${config.coordinationPort}${config.delete}")).map(_.status),
              5.seconds.dilated // As this is the first request fired via `singleRequest`, creating the pool takes some time (probably)
            )
          coordinationStatus should (be(OK) or be(NotFound))
        }
      }
    }

    enterBarrier("coordination-started")

    ConstructrExtension(system)

    val listener = system.actorOf(Props(new Actor {
      import ClusterEvent._

      var isMember = false
      Cluster(context.system).subscribe(self, InitialStateAsEvents, classOf[MemberJoined], classOf[MemberUp])

      override def receive: Receive = {
        case "isMember" => sender() ! isMember
        case MemberJoined(member) if member.address == Cluster(context.system).selfAddress => isMember = true
        case MemberUp(member) if member.address == Cluster(context.system).selfAddress => isMember = true
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

    within(5.seconds.dilated) {
      awaitAssert {
        val constructrNodes =
          Await.result(
            Http().singleRequest(Get(s"http://127.0.0.1:${config.coordinationPort}${config.get}")).flatMap(Unmarshal(_).to[String].map(config.toNodes)),
            1.second.dilated
          )
        val ports = constructrNodes.flatMap(_.port)
        ports shouldBe Range(2551, 2551 + roles.size).toSet
      }
    }

    enterBarrier("done")
  }

  override def initialParticipants: Int = roles.size
}
