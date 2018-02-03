package justin.db

import akka.cluster.Cluster
import akka.remote.testkit.MultiNodeSpec
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.Suite

object MultiNodeClusterSpec {

  val commonBaseConfig: Config = ConfigFactory.parseString(
    s"""
       |akka.loglevel = INFO
       |akka.log-config-on-start = false
       |akka.log-dead-letters = off
       |akka.log-dead-letters-during-shutdown = off
       |akka.remote.log-remote-lifecycle-events = off
    """.stripMargin
  )
}

trait MultiNodeClusterSpec extends Suite with STMultiNodeSpec { self: MultiNodeSpec ⇒

  /**
    * Get the cluster node to use.
    */
  def cluster: Cluster = Cluster(system)

  def initialParticipants: Int = roles.size
}
