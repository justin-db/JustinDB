package justin.db.client

import akka.http.scaladsl.server.directives._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{FlatSpec, Matchers}

class JustinDirectivesTest extends FlatSpec with Matchers with ScalatestRouteTest
  with RouteDirectives
  with JustinDirectives {

  behavior of "Justin Directives"

  it should "provide empty VectorClock instance when no header is passed" in {
    Get("/") ~> withVectorClockHeader(x => complete(x.vectorClock.toString) ~> check {
      responseAs[String] shouldBe VectorClockHeader.empty.vectorClock.toString
    }
  }
}
