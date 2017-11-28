package justin.db

import akka.actor.{Actor, Props}
import akka.cluster.{Cluster, ClusterEvent, MemberStatus}
import akka.pattern.ask
import akka.remote.testkit.MultiNodeSpec
import akka.testkit.TestDuration
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import de.heikoseeberger.constructr.ConstructrExtension
import org.scalatest.Suite

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
    akka.cluster.roles = [storagenode]
    constructr.coordination.host = "0.0.0.0"
    constructr.coordination.port = 2379
    """
  )
}

trait MultiNodeClusterSpec extends Suite with STMultiNodeSpec with DockerEtcd { self: MultiNodeSpec ⇒

  /**
    * Get the cluster node to use.
    */
  def cluster: Cluster = Cluster(system)

  def initialParticipants: Int = roles.size

  override protected def atStartup(): Unit = {
    runOn(roles.head) {
      startAllOrFail()
    }
  }


  override protected def afterTermination(): Unit = {
    runOn(roles.head) {
      stopAllQuietly()
    }
  }
}
