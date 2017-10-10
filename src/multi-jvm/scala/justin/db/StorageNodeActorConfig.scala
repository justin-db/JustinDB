package justin.db

import akka.remote.testconductor.RoleName
import akka.remote.testkit.MultiNodeConfig
import com.typesafe.config.ConfigFactory

case class StorageNodeActorConfig(clusterName: String) extends MultiNodeConfig {
  val node1: RoleName = role("storagenode-1")
  val node2: RoleName = role("storagenode-2")
  val node3: RoleName = role("storagenode-3")
  val node4: RoleName = role("storagenode-4")
  val node5: RoleName = role("storagenode-5")

  val nodes: List[RoleName] = List(node1, node2, node3, node4, node5)

  commonConfig(ConfigFactory.parseString(s"""
    akka {
      loggers = ["akka.event.slf4j.Slf4jLogger"]
      loglevel = "DEBUG"
      logging-filter = "akka.event.slf4j.Slf4jLoggingFilter"
      log-dead-letters-during-shutdown = off
      remote.log-remote-lifecycle-events = off
    }
    akka.actor.provider = "cluster"

    akka.netty.tcp.hostname = "127.0.0.1"

    akka.cluster.roles = [storagenode]
    akka.cluster.role.storagenode.min-nr-of-members = ${nodes.size}
    akka.cluster.seed-nodes = [ "akka.tcp://$clusterName@localhost:25551", "akka.tcp://$clusterName@localhost:25552" ]

    # don't use sigar for tests, native lib not in path
    akka.cluster.metrics.collector-class = akka.cluster.JmxMetricsCollector"""
  ))

  nodeConfig(node1)(
    ConfigFactory.parseString(
      """
        akka.remote.netty.tcp.port=25551
      """.stripMargin)
  )
  nodeConfig(node2)(
    ConfigFactory.parseString(
      """
        akka.remote.netty.tcp.port=25552
      """.stripMargin)
  )
  nodeConfig(node3)(
    ConfigFactory.parseString(
      """
        akka.remote.netty.tcp.port=0
      """.stripMargin)
  )
  nodeConfig(node4)(
    ConfigFactory.parseString(
      """
        akka.remote.netty.tcp.port=0
      """.stripMargin)
  )
  nodeConfig(node5)(
    ConfigFactory.parseString(
      """
        akka.remote.netty.tcp.port=0
      """.stripMargin)
  )
}
