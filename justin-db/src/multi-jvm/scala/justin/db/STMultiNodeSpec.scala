package justin.db

import akka.remote.testkit.MultiNodeSpecCallbacks
import org.scalatest.{BeforeAndAfterAll, FeatureSpecLike, Matchers}

trait STMultiNodeSpec extends MultiNodeSpecCallbacks
  with FeatureSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def beforeAll() = multiNodeSpecBeforeAll()
  override def afterAll()  = multiNodeSpecAfterAll()
}
