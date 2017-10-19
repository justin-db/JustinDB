package justin.db

import akka.remote.testkit.{MultiNodeConfig, MultiNodeSpec}

final class DiscoverMultiNodeConfig extends MultiNodeConfig {
  val first  = role("first")
  val second = role("second")
  val third  = role("third")
  val fourth = role("fourth")
  val fifth  = role("fifth")

  commonConfig(MultiNodeClusterSpec.clusterConfig)
}

final class MultiNodeEtcdConstructrSpecMultiJvmNode1 extends DiscoverConstructrNodesSpec
final class MultiNodeEtcdConstructrSpecMultiJvmNode2 extends DiscoverConstructrNodesSpec
final class MultiNodeEtcdConstructrSpecMultiJvmNode3 extends DiscoverConstructrNodesSpec
final class MultiNodeEtcdConstructrSpecMultiJvmNode4 extends DiscoverConstructrNodesSpec
final class MultiNodeEtcdConstructrSpecMultiJvmNode5 extends DiscoverConstructrNodesSpec

abstract class DiscoverConstructrNodesSpec(config: DiscoverMultiNodeConfig)
  extends MultiNodeSpec(config)
  with MultiNodeClusterSpec {

  def this() = this(new DiscoverMultiNodeConfig())

  "ConstructR should manage an Akka cluster" in {
    awaitConstructRClusterUp()
    enterBarrier("akka-cluster-up")
  }
}
