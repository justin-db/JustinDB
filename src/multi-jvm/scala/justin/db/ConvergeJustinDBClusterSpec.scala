package justin.db

import akka.remote.testkit.{MultiNodeConfig, MultiNodeSpec}
import com.typesafe.config.ConfigFactory

final class ConvergeJustinDBClusterConfig extends MultiNodeConfig {
  val first  = role("first")
  val second = role("second")
  val third  = role("third")

  private[this] val allRoles  = List(first, second, third)
  private[this] val clusterName = "ConvergeJustinDBClusterSpec"

  private[this] def commonNodeConfig(id: Int) = ConfigFactory.parseString(
    s"""
       |justin.system                                   = $clusterName
       |justin.kubernetes-hostname                      = s"justindb-$id"
       |justin.http.port                                = ${9000 + id}
       |akka.cluster.role.storagenode.min-nr-of-members = ${allRoles.size}
       |akka.cluster.http.management.port               = ${19999 + id}
       |akka.cluster.seed-nodes.0                       = "akka.tcp://$clusterName@localhost:25551"
       |akka.remote.netty.tcp.port                      = ${25551 + id}
       |akka.remote.netty.tcp.hostname                  = "localhost"
    """.stripMargin
  )

  commonConfig(MultiNodeClusterSpec.commonBaseConfig.withFallback(JustinDBConfig.init.config))

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

  "A cluster" must {
    "be able to form" in {
      val config = new JustinDBConfig(system.settings.config)
      val justinDB = JustinDB.init(config)(system)

      enterBarrier("justindb-cluster-up")
    }
  }
}
