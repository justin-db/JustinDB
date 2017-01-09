package justin.http_client

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import justin.db.entropy.ActiveAntiEntropyActorRef
import org.scalatest.{FlatSpec, Matchers}

class ActiveAntiEntropyRouterTest extends FlatSpec with Matchers with ScalatestRouteTest {

  behavior of "Active-Anti-Entropy Router"

  it should "post run active-anti-entropy mechanism" in {
    Post("/aae/run") ~> Route.seal(new ActiveAntiEntropyRouter(ActiveAntiEntropyActorRef(null)).routes) ~> check {
      status shouldBe StatusCodes.NoContent
    }
  }
}
