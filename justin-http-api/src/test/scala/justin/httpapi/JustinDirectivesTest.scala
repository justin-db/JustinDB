package justin.httpapi

import akka.http.scaladsl.server.directives._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import justin.db.consistenthashing.NodeId
import justin.db.vectorclocks.{Counter, VectorClock}
import org.scalatest.{FlatSpec, Matchers}

class JustinDirectivesTest extends FlatSpec with Matchers with ScalatestRouteTest
  with RouteDirectives
  with JustinDirectives {

  behavior of "Justin Directives"

  it should "provide empty VectorClock instance when no header is passed" in {
    Get("/") ~> withVectorClockHeader(x => complete(x.vectorClock.toString)) ~> check {
      responseAs[String] shouldBe VectorClockHeader.empty.vectorClock.toString
    }
  }

  it should "provide instance of VectorClock build upon passed header" in {
    val vClock = VectorClock(Map(NodeId(1) -> Counter(1), NodeId(2) -> Counter(2), NodeId(3) -> Counter(9)))
    val header = VectorClockHeader(vClock)

    Get("/").addHeader(header) ~> withVectorClockHeader(x => complete(x.vectorClock.toString)) ~> check {
      responseAs[String] shouldBe vClock.toString
    }
  }
}
