package justin.http_api

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import justin.db.actors.StorageNodeActorRef
import justin.db.actors.protocol.MultiDataCenterContacts
import spray.json.DefaultJsonProtocol._

import scala.concurrent.ExecutionContext

class ClusterClientRouter(storageNodeActor: StorageNodeActorRef)(implicit ec: ExecutionContext, mat: Materializer, system: ActorSystem) {

  def routes: Route = path("cluster-client" / "contacts") {
    (post & pathEndOrSingleSlash & entity(as[List[String]])) { initialContacts =>
      storageNodeActor.ref ! MultiDataCenterContacts(initialContacts)
      complete(StatusCodes.NoContent)
    }
  }
}
