package justin.http_api

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import justin.db.replica.multidatacenter.InitialContactsValidator
import spray.json.DefaultJsonProtocol._

import scala.concurrent.ExecutionContext

class ClusterClientRouter(initialContactsValidator: InitialContactsValidator)(implicit ec: ExecutionContext, mat: Materializer, system: ActorSystem) {

  def routes: Route = path("cluster-client" / "contacts") {
    (post & pathEndOrSingleSlash & entity(as[List[String]])) { initialContacts =>
      initialContactsValidator.apply(initialContacts) match {
        case InitialContactsValidator.NotValid => complete(StatusCodes.BadRequest)
        case InitialContactsValidator.OK       => complete(StatusCodes.NoContent)
      }
    }
  }
}
