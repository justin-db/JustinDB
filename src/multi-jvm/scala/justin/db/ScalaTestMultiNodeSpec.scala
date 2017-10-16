package justin.db

import akka.remote.testkit.MultiNodeSpecCallbacks
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

trait ScalaTestMultiNodeSpec extends MultiNodeSpecCallbacks
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def beforeAll(): Unit = multiNodeSpecBeforeAll()
  override def afterAll(): Unit = multiNodeSpecAfterAll()
}
