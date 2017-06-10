package justin.httpapi

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{FlatSpec, Matchers}

class HealthCheckRouterTest extends FlatSpec with Matchers with ScalatestRouteTest {

  behavior of "Health Check Router"

  it should "get 200 OK http status" in {
    Get("/health") ~> Route.seal(new HealthCheckRouter().routes) ~> check {
      status             shouldBe StatusCodes.OK
      responseAs[String] shouldBe "OK"
    }
  }
}
