package justin.db.client

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.Materializer

import scala.concurrent.ExecutionContext

class BuildInfoRouter(implicit ec: ExecutionContext, mat: Materializer) {

  val routes: Route = path("info") {
    get {
      complete(HttpResponse(entity = HttpEntity(ContentType(MediaTypes.`application/json`), BuildInfo.toJson)))
    }
  }
}
