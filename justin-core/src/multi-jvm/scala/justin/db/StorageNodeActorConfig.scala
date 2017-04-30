package justin.db

import akka.remote.testconductor.RoleName
import akka.remote.testkit.MultiNodeConfig
import com.typesafe.config.ConfigFactory

object StorageNodeActorConfig extends MultiNodeConfig {
  val seed: RoleName  = role("StorageNode-seed")
  val node1: RoleName = role("StorageNode-1")
  val node2: RoleName = role("StorageNode-2")

  commonConfig(ConfigFactory.parseString("""
    akka.actor.provider="akka.cluster.ClusterActorRefProvider"
    # don't use sigar for tests, native lib not in path
    akka.cluster.metrics.collector-class = akka.cluster.JmxMetricsCollector"""
  ))
}
