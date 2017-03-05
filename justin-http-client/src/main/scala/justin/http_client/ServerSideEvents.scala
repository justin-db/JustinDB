package justin.http_client

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{BroadcastHub, Keep, Source, SourceQueueWithComplete}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import de.heikoseeberger.akkasse.ServerSentEvent

import scala.concurrent.ExecutionContext

class ServerSideEvents()(implicit ec: ExecutionContext, as: ActorSystem, am: ActorMaterializer) {
  import akka.http.scaladsl.server.Directives._
  import de.heikoseeberger.akkasse.EventStreamMarshalling._ // That does the trick!

  // scalastyle:off magic.number
  private val (in: SourceQueueWithComplete[String], out: Source[String, NotUsed]) = {
    Source.queue[String](100, OverflowStrategy.dropHead)
      .toMat(BroadcastHub.sink[String])(Keep.both)
      .run()
  }
  // scalastyle:on magic.number

  def routes: Route = path("events") {
    get {
      complete {
        out.map(ServerSentEvent.apply)
      }
    }
  }
}
