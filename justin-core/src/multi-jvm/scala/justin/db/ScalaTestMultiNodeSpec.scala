package justin.db

import akka.remote.testkit.MultiNodeSpecCallbacks
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

trait ScalaTestMultiNodeSpec extends MultiNodeSpecCallbacks
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def beforeAll() = multiNodeSpecBeforeAll()
  override def afterAll()  = multiNodeSpecAfterAll()
}
