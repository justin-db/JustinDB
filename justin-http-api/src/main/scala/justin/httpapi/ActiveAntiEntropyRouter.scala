package justin.httpapi

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import justin.db.entropy.{ActiveAntiEntropyActorRef, ActiveAntiEntropyProtocol}

class ActiveAntiEntropyRouter(actorRef: ActiveAntiEntropyActorRef) {

  val routes: Route = path("aae" / "run") {
    post {
      actorRef.ref ! ActiveAntiEntropyProtocol.Run
      complete(StatusCodes.NoContent)
    }
  }
}
