package justin.db.client

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import justin.db.storage.InMemStorage
import justin.db.{StorageNodeActor, StorageNodeActorId}

object Main extends App {
  val config = ConfigFactory.parseString(s"akka.cluster.roles = [${StorageNodeActor.role}]")
    .withFallback(ConfigFactory.load())

  implicit val system       = ActorSystem("justin-db-cluster-system", config)
  implicit val executor     = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val logger = Logging(system, getClass)

  val storage             = new InMemStorage() // TODO: make it configurable
  val nodeId              = StorageNodeActorId(config.getInt("node.id"))
  val storageNodeActorRef = system.actorOf(StorageNodeActor.props(nodeId, storage), name = StorageNodeActor.name(nodeId))
  val client              = new HttpStorageNodeClient(storageNodeActorRef)
  val router              = new StorageNodeRouter(client)

  Http()
    .bindAndHandle(router.routes, config.getString("http.interface"), config.getInt("http.port"))
    .map { binding => logger.info(s"HTTP server started at ${binding.localAddress}") }
    .recover { case ex => logger.error(ex, "Could not start HTTP server") }
}
