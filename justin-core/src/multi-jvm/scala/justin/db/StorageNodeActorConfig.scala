package justin.db

import akka.remote.testconductor.RoleName
import akka.remote.testkit.MultiNodeConfig
import com.typesafe.config.ConfigFactory

object StorageNodeActorConfig extends MultiNodeConfig {
  val seed: RoleName  = role("storagenode-seed")
  val node1: RoleName = role("storagenode-1")
  val node2: RoleName = role("storagenode-2")

  commonConfig(ConfigFactory.parseString("""
    akka {
      loggers = ["akka.event.slf4j.Slf4jLogger"]
      loglevel = "DEBUG"
      logging-filter = "akka.event.slf4j.Slf4jLoggingFilter"
      log-dead-letters-during-shutdown = off
      remote.log-remote-lifecycle-events = off
    }
    akka.actor.provider = "cluster"

    akka.netty.tcp.hostname = "127.0.0.1"

    # don't use sigar for tests, native lib not in path
    akka.cluster.metrics.collector-class = akka.cluster.JmxMetricsCollector"""
  ))

  nodeConfig(seed)(
    ConfigFactory.parseString(
      """
        akka.remote.netty.tcp.port=25521
        node.id=1
      """.stripMargin)
  )
  nodeConfig(node1)(
    ConfigFactory.parseString(
      """
        akka.remote.netty.tcp.port=25522
        node.id=2
      """.stripMargin)
  )
  nodeConfig(node2)(
    ConfigFactory.parseString(
      """
        akka.remote.netty.tcp.port=25523
        node.id=3
      """.stripMargin)
  )
}
