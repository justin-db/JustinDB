package justin.http_api

import akka.actor.{ActorPath, ActorSystem}
import akka.cluster.client.{ClusterClient, ClusterClientSettings}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.concurrent.ExecutionContext

object ClusterClientRouter {
  case class InitialContacts(contacts: List[String])
  implicit val initialContactsFormat: RootJsonFormat[InitialContacts] = jsonFormat1(InitialContacts)
}

class ClusterClientRouter()(implicit ec: ExecutionContext, mat: Materializer, system: ActorSystem) {
  import ClusterClientRouter._

  def routes: Route = path("cluster-client" / "contacts") {
    (post & pathEndOrSingleSlash & entity(as[InitialContacts])) { initialContacts =>
      val contacts = initialContacts.contacts.map(ActorPath.fromString).toSet
      val settings = ClusterClientSettings(system).withInitialContacts(contacts)
      system.actorOf(ClusterClient.props(settings), "client")

      complete(StatusCodes.NoContent)
    }
  }
}
