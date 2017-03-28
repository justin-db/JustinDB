package justin

import akka.actor.ActorSystem
import akka.cluster.Cluster
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import buildinfo.BuildInfo
import com.typesafe.config.{Config, ConfigFactory}
import justin.consistent_hashing.{NodeId, Ring}
import justin.db.actors.{StorageNodeActor, StorageNodeActorRef}
import justin.db.client.ActorRefStorageNodeClient
import justin.db.entropy.{ActiveAntiEntropyActor, ActiveAntiEntropyActorRef}
import justin.db.replication.N
import justin.db.storage.JustinDriver
import justin.http_api._

// $COVERAGE-OFF$
object Main extends App with ServiceConfig {

  override protected val config: Config = ConfigFactory
    .parseString(s"akka.cluster.roles = [${StorageNodeActor.role}]")
    .withFallback(ConfigFactory.load())

  implicit val system       = ActorSystem("justin-db-cluster-system", config)
  implicit val executor     = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val logger = Logging(system, getClass)

  logger.info("JustinDB version: " + BuildInfo.version)
  logger.info("Build Info: ")
  logger.info(BuildInfo.toString)

  val storage = JustinDriver.load(`storage-backend`)

  Cluster(system).registerOnMemberUp {
    logger.info("Cluster is ready!")

    // STORAGE ACTOR
    val storageNodeActorRef = new ActorRefStorageNodeClient(StorageNodeActorRef {
      val nodeId      = NodeId(`nodes-id`)
      val ring        = Ring(`ring-cluster-size`, `ring-partitions`)
      val replication = N(`replication-n`)

      system.actorOf(
        props = StorageNodeActor.props(nodeId, storage, ring, replication),
        name  = StorageNodeActor.name(nodeId)
      )
    })

    // ENTROPY ACTOR
    val activeAntiEntropyActorRef = ActiveAntiEntropyActorRef(system.actorOf(ActiveAntiEntropyActor.props))

    // HTTP API
    val routes = logRequestResult(system.name) {
        new HttpRouter(storageNodeActorRef).routes ~
        new HealthCheckRouter().routes ~
        new BuildInfoRouter().routes(BuildInfo.toJson) ~
        new ActiveAntiEntropyRouter(activeAntiEntropyActorRef).routes ~
        new ServerSideEvents().routes
    }

    Http()
      .bindAndHandle(routes, `http-interface`, `http-port`)
      .map { binding => logger.info(s"HTTP server started at ${binding.localAddress}") }
      .recover { case ex => logger.error(ex, "Could not start HTTP server") }
  }
}
// $COVERAGE-ON$
