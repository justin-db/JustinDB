package justin.http_client

import akka.actor.Actor
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestActorRef
import justin.db.entropy.{ActiveAntiEntropyActorRef, ActiveAntiEntropyProtocol}
import justin.http_client.ActiveAntiEntropyRouterTest.StubActiveAntiEntropyActor
import org.scalatest.{FlatSpec, Matchers}

class ActiveAntiEntropyRouterTest extends FlatSpec with Matchers with ScalatestRouteTest {

  behavior of "Active-Anti-Entropy Router"

  it should "post run active-anti-entropy mechanism" in {
    val router = new ActiveAntiEntropyRouter(ActiveAntiEntropyActorRef(TestActorRef[StubActiveAntiEntropyActor]))
    Post("/aae/run") ~> Route.seal(router.routes) ~> check {
      status shouldBe StatusCodes.NoContent
    }
  }
}

object ActiveAntiEntropyRouterTest {
  private class StubActiveAntiEntropyActor extends Actor {
    override def receive: Receive = {
      case ActiveAntiEntropyProtocol.Run => println("starting operation of solving cluster data entropy")
    }
  }
}