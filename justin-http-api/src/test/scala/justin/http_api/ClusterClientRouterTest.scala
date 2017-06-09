package justin.http_api

import akka.actor.Actor
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import justin.db.actors.StorageNodeActorRef
import justin.db.replica.multidatacenter.InitialContactsValidator
import justin.db.replica.multidatacenter.InitialContactsValidator.InitialContactsResponse
import org.scalatest.{FlatSpec, Matchers}
import spray.json.DefaultJsonProtocol._

class ClusterClientRouterTest extends FlatSpec with Matchers with ScalatestRouteTest {

  behavior of "Cluster Client Router"

  it should "accept initial contacts" in {
    val contacts = List(
      "akka.tcp://OtherSys@host1:2552/system/receptionist",
      "akka.tcp://OtherSys@host2:2552/system/receptionist"
    )
    val contactsValidator = new InitialContactsValidator(StorageNodeActorRef(Actor.noSender)) {
      override def apply(initialContacts: List[String]): InitialContactsResponse = InitialContactsValidator.OK
    }
    Post("/cluster-client/contacts", contacts) ~> Route.seal(new ClusterClientRouter(contactsValidator).routes) ~> check {
      status shouldBe StatusCodes.NoContent
    }
  }

  it should "NOT accept initial contacts" in {
    val contacts = List.empty[String]
    val contactsValidator = new InitialContactsValidator(StorageNodeActorRef(Actor.noSender)) {
      override def apply(initialContacts: List[String]): InitialContactsResponse = InitialContactsValidator.NotValid
    }
    Post("/cluster-client/contacts", contacts) ~> Route.seal(new ClusterClientRouter(contactsValidator).routes) ~> check {
      status shouldBe StatusCodes.BadRequest
    }
  }
}
