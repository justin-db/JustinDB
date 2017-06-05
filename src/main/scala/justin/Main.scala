package justin

import akka.actor.{Actor, ActorLogging, ActorPath, ActorRef, ActorSystem, Props}
import akka.cluster.client.{ClusterClient, ClusterClientReceptionist, ClusterClientSettings}
import akka.cluster.Cluster
import akka.cluster.http.management.ClusterHttpManagementRoutes
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import buildinfo.BuildInfo
import com.typesafe.config.ConfigFactory
import justin.db.actors.{StorageNodeActor, StorageNodeActorRef}
import justin.db.client.ActorRefStorageNodeClient
import justin.db.consistenthashing.{NodeId, Ring}
import justin.db.entropy.{ActiveAntiEntropyActor, ActiveAntiEntropyActorRef}
import justin.db.replica.N
import justin.db.storage.JustinDriver
import justin.http_api.ClusterClientRouter.InitialContacts
import justin.httpapi._

import scala.language.reflectiveCalls

// $COVERAGE-OFF$
object Main extends App {

  val justinConfig = JustinConfig(ConfigFactory
    .parseString(s"akka.cluster.roles = [${StorageNodeActor.role}]")
    .withFallback(ConfigFactory.load()))

  implicit val system       = ActorSystem(justinConfig.system, justinConfig.config)
  implicit val executor     = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val logger = Logging(system, getClass)

  val storage = JustinDriver.load(justinConfig.`storage-type`)

  logger.info(
    """
      |   ___              _    _        ______ ______
      |  |_  |            | |  (_)       |  _  \| ___ \
      |    | | _   _  ___ | |_  _  _ __  | | | || |_/ /
      |    | || | | |/ __|| __|| || '_ \ | | | || ___ \
      |/\__/ /| |_| |\__ \| |_ | || | | || |/ / | |_/ /
      |\____/  \__,_||___/ \__||_||_| |_||___/  \____/
      |
    """.stripMargin)

  logger.info("Build Info: " + BuildInfo.toString)
  logger.info("Properties: ")
  logger.info("-- Storage: "            + storage.name)
  logger.info("-- NodeId: "             + justinConfig.`node-id`)
  logger.info("-- Cluster size: "       + justinConfig.ring.`members-count`)
  logger.info("-- Partitions number: "  + justinConfig.ring.partitions)
  logger.info("-- Replication factor: " + justinConfig.replication.N)
  logger.info("-- ConstructR coordination host: " + justinConfig.config.getString("constructr.coordination.host"))

  val cluster = Cluster(system)

  cluster.registerOnMemberUp {
    logger.info("Cluster is ready!")

    // STORAGE ACTOR
    val storageNodeActorRef = StorageNodeActorRef {
      val nodeId = NodeId(justinConfig.`node-id`)
      val ring   = Ring(justinConfig.ring.`members-count`, justinConfig.ring.partitions)
      val n      = N(justinConfig.replication.N)

      system.actorOf(
        props = StorageNodeActor.props(nodeId, storage, ring, n),
        name  = StorageNodeActor.name(nodeId)
      )
    }

    // CLUSTER CLIENT RECEPTIONIST
    ClusterClientReceptionist(system).registerService(storageNodeActorRef.ref)

    class ClusterClientStorage extends Actor with ActorLogging {

      private var clusterClient: Option[ActorRef] = None

      def receive = {
        case initialContacts: InitialContacts =>
          println("got contacts: " + initialContacts)
          val contacts = initialContacts.contacts.map(ActorPath.fromString).toSet
          println("actor paths: " + contacts)
          val settings = ClusterClientSettings.apply(system = system).withInitialContacts(contacts)
          clusterClient = Some(system.actorOf(ClusterClient.props(settings), "client"))
          println("clusterClient: " + clusterClient)
        case t: String =>
          println("sedning msg: " + t)
          println("clusterClient: " + clusterClient)
          clusterClient.foreach { ref =>
            println("sending via cluster client")
            ref ! ClusterClient.Send("/user/id-0", t, localAffinity = false)
          }
        case e =>
          println("nieoczekiwana wiadomosc: " + e)
      }
    }
    val clusterClientStorage = system.actorOf(Props[ClusterClientStorage], "clusterClientStorage")

    // ENTROPY ACTOR
    val activeAntiEntropyActorRef = ActiveAntiEntropyActorRef(system.actorOf(ActiveAntiEntropyActor.props))

    // HTTP API
    val routes = logRequestResult(system.name) {
      new HttpRouter(new ActorRefStorageNodeClient(storageNodeActorRef)).routes ~
      new HealthCheckRouter().routes ~
      new BuildInfoRouter().routes(BuildInfo.toJson) ~
      new ActiveAntiEntropyRouter(activeAntiEntropyActorRef).routes ~
      new ServerSideEvents().routes ~
      new ClusterClientRouter(clusterClientStorage).routes ~ ClusterHttpManagementRoutes.apply(cluster, pathPrefixName = "cluster")
    }

    Http()
      .bindAndHandle(routes, justinConfig.http.interface, justinConfig.http.port)
      .map { binding => logger.info(s"HTTP server started at ${binding.localAddress}") }
      .recover { case ex => logger.error(ex, "Could not start HTTP server") }
  }
}
// $COVERAGE-ON$
