package justin.db

import justin.db.actors.RoundRobinCoordinatorRouter
import org.scalatest.{FlatSpec, Matchers}

class RoundRobinCoordinatorRouterTest extends FlatSpec with Matchers {

  behavior of "Round-Robin coordinator Router"

  it should "get router name" in {
    RoundRobinCoordinatorRouter.routerName shouldBe "CoordinatorRouter"
  }
}
