package justin.http_client

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{FlatSpec, Matchers}

class BuildInfoRouterTest extends FlatSpec with Matchers with ScalatestRouteTest {

  behavior of "Build Info Router"

  it should "get 200 OK http status along with system build info" in {
    Get("/info") ~> Route.seal(new BuildInfoRouter().routes) ~> check {
      status             shouldBe StatusCodes.OK
      responseAs[String] shouldBe BuildInfo.toJson
    }
  }
}
