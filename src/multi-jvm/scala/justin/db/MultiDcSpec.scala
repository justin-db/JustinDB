package justin.db

import akka.cluster.MemberStatus
import akka.remote.testkit.{MultiNodeConfig, MultiNodeSpec}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

final class MultiDcSpecConfig(crossDcConnections: Int = 5) extends MultiNodeConfig {
  val first  = role("first")
  val second = role("second")
  val third  = role("third")
  val fourth = role("fourth")
  val fifth  = role("fifth")

  commonConfig(ConfigFactory.parseString(
    s"""
      akka.cluster.multi-data-center.cross-data-center-connections = $crossDcConnections
    """).withFallback(MultiNodeClusterSpec.clusterConfig)
  )

  nodeConfig(first, second)(ConfigFactory.parseString(
    """
      akka.cluster.multi-data-center.self-data-center = "dc1"
    """))

  nodeConfig(third, fourth, fifth)(ConfigFactory.parseString(
    """
      akka.cluster.multi-data-center.self-data-center = "dc2"
    """))
}

final class MultiDcMultiJvm1 extends MultiDcSpec
final class MultiDcMultiJvm2 extends MultiDcSpec
final class MultiDcMultiJvm3 extends MultiDcSpec
final class MultiDcMultiJvm4 extends MultiDcSpec
final class MultiDcMultiJvm5 extends MultiDcSpec

abstract class MultiDcSpec(config: MultiDcSpecConfig)
  extends MultiNodeSpec(config)
    with MultiNodeClusterSpec {

  import config._

  def this() = this(new MultiDcSpecConfig())

  "A cluster with multiple data centers" must {
    "be able to form" in {

      awaitConstructRClusterUp()
      enterBarrier("form-cluster-join-attempt")

      runOn(first, second, third, fourth, fifth) {
        within(20.seconds) {
          awaitAssert(cluster.state.members.filter(_.status == MemberStatus.Up) should have size (initialParticipants))
        }
      }

      enterBarrier("cluster started")
    }
  }
}
