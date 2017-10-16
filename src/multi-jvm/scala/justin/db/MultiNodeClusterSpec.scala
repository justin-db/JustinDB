package justin.db

import akka.remote.testkit.MultiNodeSpec
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.Suite

object MultiNodeClusterSpec {

  def clusterConfig: Config = ConfigFactory.parseString(s"""
    akka.actor.provider = cluster
    akka.actor.warn-about-java-serializer-usage = off
    akka.loglevel = INFO
    akka.log-dead-letters = off
    akka.log-dead-letters-during-shutdown = off
    akka.remote {
      log-remote-lifecycle-events = off
    }
    akka.loggers = ["akka.testkit.TestEventListener"]
    akka.test {
      single-expect-default = 5 s
    }
    """
  )
}

trait MultiNodeClusterSpec extends Suite with ScalaTestMultiNodeSpec { self: MultiNodeSpec ⇒
  def initialParticipants = roles.size
}
