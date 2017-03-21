package justin.http_api

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.Materializer
import buildinfo.BuildInfo

import scala.concurrent.ExecutionContext

class BuildInfoRouter(implicit ec: ExecutionContext, mat: Materializer) {

  val routes: Route = path("info") {
    get {
      complete(HttpResponse(entity = HttpEntity(ContentType(MediaTypes.`application/json`), BuildInfo.toJson)))
    }
  }
}
