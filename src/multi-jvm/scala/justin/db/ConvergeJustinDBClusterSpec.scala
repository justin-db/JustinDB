package justin.db

import akka.remote.testkit.{MultiNodeConfig, MultiNodeSpec}

final class ConvergeJustinDBClusterConfig extends MultiNodeConfig {
  val first  = role("first")
  val second = role("second")
  val third  = role("third")
  val fourth = role("fourth")
  val fifth  = role("fifth")

  commonConfig(MultiNodeClusterSpec.clusterConfig)
}

final class ConvergeJustinDBClusterSpecMultiJvmNode1 extends ConvergeJustinDBClusterSpec
final class ConvergeJustinDBClusterSpecMultiJvmNode2 extends ConvergeJustinDBClusterSpec
final class ConvergeJustinDBClusterSpecMultiJvmNode3 extends ConvergeJustinDBClusterSpec
final class ConvergeJustinDBClusterSpecMultiJvmNode4 extends ConvergeJustinDBClusterSpec
final class ConvergeJustinDBClusterSpecMultiJvmNode5 extends ConvergeJustinDBClusterSpec

abstract class ConvergeJustinDBClusterSpec(config: ConvergeJustinDBClusterConfig)
  extends MultiNodeSpec(config)
  with MultiNodeClusterSpec {

  def this() = this(new ConvergeJustinDBClusterConfig())

  "ConstructR should manage an Akka cluster" in {
    awaitConstructRClusterUp()
  }

}
