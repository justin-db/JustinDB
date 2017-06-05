package justin.http_api

import akka.actor.Actor
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import justin.db.actors.StorageNodeActorRef
import org.scalatest.{FlatSpec, Matchers}
import spray.json.DefaultJsonProtocol._

class ClusterClientRouterTest extends FlatSpec with Matchers with ScalatestRouteTest {

  behavior of "Cluster Client Router"

  it should "accept initial contacts" in {
    val contacts = List(
      "akka.tcp://OtherSys@host1:2552/system/receptionist",
      "akka.tcp://OtherSys@host2:2552/system/receptionist"
    )

    Post("/cluster-client/contacts", contacts) ~> Route.seal(new ClusterClientRouter(StorageNodeActorRef(Actor.noSender)).routes) ~> check {
      status shouldBe StatusCodes.NoContent
    }
  }
}
