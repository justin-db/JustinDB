package justin.db

import akka.remote.testkit.{MultiNodeConfig, MultiNodeSpec}
import com.typesafe.config.ConfigFactory

final class ConvergeJustinDBClusterConfig extends MultiNodeConfig with DockerEtcd {
  val first  = role("first")
  val second = role("second")
  val third  = role("third")

  private[this] val allRoles  = List(first, second, third)
  private[this] val clusterName = "ConvergeJustinDBClusterSpec"
  private[this] val commonBaseConfig = ConfigFactory.parseString(
    s"""
       |akka.loglevel = INFO
       |akka.log-dead-letters = off
       |akka.log-dead-letters-during-shutdown = off
       |akka.remote.log-remote-lifecycle-events = off
    """.stripMargin
  )
  private[this] def commonNodeConfig(id: Int) = ConfigFactory.parseString(
    s"""
       |justin.system = $clusterName
       |justin.node-id = $id
       |justin.http.port = ${9000 + id}
       |akka.cluster.role.storagenode.min-nr-of-members = ${allRoles.size}
       |akka.remote.netty.tcp.port = 0
       |akka.remote.netty.tcp.hostname = "localhost"
       |akka.remote.netty.tcp.bind-hostname = "0.0.0.0"
       |akka.remote.netty.tcp.bind-port = 0
       |akka.cluster.http.management.port = ${19999 + id}
    """.stripMargin
  )

  commonConfig(commonBaseConfig.withFallback(JustinDBConfig.init.config))

  allRoles.zipWithIndex.foreach { case (roleName, id) =>
    nodeConfig(roleName)(commonNodeConfig(id))
  }
}

final class ConvergeJustinDBClusterSpecMultiJvmNode1 extends ConvergeJustinDBClusterSpec
final class ConvergeJustinDBClusterSpecMultiJvmNode2 extends ConvergeJustinDBClusterSpec
final class ConvergeJustinDBClusterSpecMultiJvmNode3 extends ConvergeJustinDBClusterSpec

abstract class ConvergeJustinDBClusterSpec(config: ConvergeJustinDBClusterConfig)
  extends MultiNodeSpec(config)
  with MultiNodeClusterSpec {

  def this() = this(new ConvergeJustinDBClusterConfig())

  "ConstructR should manage an Akka cluster" in {
    val config = new JustinDBConfig(system.settings.config)
    val justinDB = JustinDB.init(config)

    enterBarrier("akka-cluster-up")
  }
}
