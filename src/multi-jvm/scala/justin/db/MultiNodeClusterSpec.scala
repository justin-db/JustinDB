package justin.db

import akka.actor.{Actor, Props}
import akka.pattern.ask
import akka.cluster.{Cluster, ClusterEvent, MemberStatus}
import akka.remote.testkit.MultiNodeSpec
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import de.heikoseeberger.constructr.ConstructrExtension
import org.scalatest.Suite
import akka.testkit.TestDuration
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object MultiNodeClusterSpec {

  def clusterConfig: Config = ConfigFactory.parseString(s"""
    akka.actor.provider = cluster
    akka.actor.warn-about-java-serializer-usage = off
    akka.loglevel = INFO
    akka.log-dead-letters = off
    akka.log-dead-letters-during-shutdown = off
    akka.loggers = ["akka.testkit.TestEventListener"]
    akka.remote.log-remote-lifecycle-events = off
    akka.test.single-expect-default = 5 s
    akka.remote.netty.tcp.hostname = "127.0.0.1"
    constructr.coordination.host = "127.0.0.1"
    constructr.coordination.port = 2379
    """
  )
}

trait MultiNodeClusterSpec extends Suite with ScalaTestMultiNodeSpec { self: MultiNodeSpec ⇒

  /**
    * Get the cluster node to use.
    */
  def cluster: Cluster = Cluster(system)

  def initialParticipants: Int = roles.size

  def awaitConstructRClusterUp(): Unit = {
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
}
