package justin.db

import akka.remote.testconductor.RoleName
import akka.remote.testkit.{MultiNodeConfig, MultiNodeSpec}
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
import akka.testkit.TestDuration

final class SplitBrainResolverConfig extends MultiNodeConfig with DockerEtcd {
  val first: RoleName  = role("first")
  val second: RoleName = role("second")
  val third: RoleName  = role("third")
  val fourth: RoleName = role("fourth")
  val fifth: RoleName  = role("fifth")

  private[this] val allRoles    = List(first, second, third, fourth, fifth)
  private[this] val clusterName = "SplitBrainResolverSpec"

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

  testTransport(on = true)

  commonConfig {
    MultiNodeClusterSpec
      .commonBaseConfig
      .withFallback(JustinDBConfig.init.config)
  }

  allRoles.zipWithIndex.foreach { case (roleName, id) =>
    nodeConfig(roleName)(commonNodeConfig(id))
  }
}

final class SplitBrainResolverSpecMultiJvmNode1 extends SplitBrainResolverSpec
final class SplitBrainResolverSpecMultiJvmNode2 extends SplitBrainResolverSpec
final class SplitBrainResolverSpecMultiJvmNode3 extends SplitBrainResolverSpec
final class SplitBrainResolverSpecMultiJvmNode4 extends SplitBrainResolverSpec
final class SplitBrainResolverSpecMultiJvmNode5 extends SplitBrainResolverSpec

abstract class SplitBrainResolverSpec(config: SplitBrainResolverConfig)
  extends MultiNodeSpec(config)
  with MultiNodeClusterSpec {
  import config._

  def this() = this(new SplitBrainResolverConfig())

  "The majority leader in a 5 node cluster" must {

    "be able to DOWN a 'last' node that is UNREACHABLE" in within(150.seconds) {
      val config = new JustinDBConfig(system.settings.config)
      val justinDB = JustinDB.init(config)

      enterBarrier("justindb-cluster-up")

      val fifthAddress = address(fifth)

      runOn(first) {
        // kill 'fifth' node
        testConductor.exit(fifth, 0).await
        enterBarrier("down-fifth-node")

        // --- HERE THE LEADER SHOULD DETECT FAILURE AND AUTO-DOWN THE UNREACHABLE NODE ---

        awaitMembersUp(numberOfMembers = 4, canNotBePartOfMemberRing = Set(fifthAddress), 30.seconds.dilated)
      }

      runOn(fifth) {
        enterBarrier("down-fifth-node")
      }

      runOn(second, third, fourth) {
        enterBarrier("down-fifth-node")

        awaitMembersUp(numberOfMembers = 4, canNotBePartOfMemberRing = Set(fifthAddress), 30.seconds.dilated)
      }

      enterBarrier("await-completion-1")
    }
  }
}
