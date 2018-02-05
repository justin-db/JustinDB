package justin.db

import akka.actor.ActorSystem
import akka.cluster.Cluster
import akka.cluster.http.management.ClusterHttpManagement
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.{ActorMaterializer, Materializer}
import buildinfo.BuildInfo
import com.typesafe.scalalogging.StrictLogging
import justin.db.actors.{StorageNodeActor, StorageNodeActorRef}
import justin.db.client.ActorRefStorageNodeClient
import justin.db.cluster.datacenter.Datacenter
import justin.db.consistenthashing.{NodeId, Ring}
import justin.db.replica.N
import justin.db.storage.PluggableStorageProtocol
import justin.db.storage.provider.StorageProvider
import justin.httpapi.{BuildInfoRouter, HealthCheckRouter, HttpRouter}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Promise}
import scala.language.reflectiveCalls

// $COVERAGE-OFF$
final class JustinDB

object JustinDB extends StrictLogging {

  private[this] def validConfiguration(justinDBConfig: JustinDBConfig): Unit = {
    require(justinDBConfig.replication.N > 0, "replication N factor can't be smaller or equal 0")
    require(justinDBConfig.ring.`members-count` > 0, "members-counter can't be smaller or equal 0")
    require(justinDBConfig.ring.partitions > 0, "ring partitions can't be smaller or equal 0")
    require(justinDBConfig.ring.partitions >= justinDBConfig.ring.`members-count`, "number of ring partitions can't be smaller than number of members-count")
    require(justinDBConfig.replication.N <= justinDBConfig.ring.`members-count`, "replication N factor can't be bigger than defined members-count number")
  }

  private[this] def initStorage(justinConfig: JustinDBConfig) = {
    val provider = StorageProvider.apply(justinConfig.storage.provider)
    logger.info("Storage provider: " + provider.name)
    provider.init
  }

  def init(justinConfig: JustinDBConfig)(implicit actorSystem: ActorSystem): JustinDB = {
    validConfiguration(justinConfig)

    val processOrchestrator = Promise[JustinDB]

    implicit val executor: ExecutionContext = actorSystem.dispatcher
    implicit val materializer: Materializer = ActorMaterializer()

    val storage: PluggableStorageProtocol = initStorage(justinConfig)

    val cluster = Cluster(actorSystem)

    cluster.registerOnMemberUp {
      // STORAGE ACTOR
      val storageNodeActorRef = StorageNodeActorRef {
        val nodeId     = NodeId(justinConfig.`kubernetes-hostname`.split("-").last.toInt)
        val ring       = Ring(justinConfig.ring.`members-count`, justinConfig.ring.partitions)
        val n          = N(justinConfig.replication.N)
        val datacenter = Datacenter(justinConfig.dc.`self-data-center`)

        actorSystem.actorOf(
          props = StorageNodeActor.props(nodeId, datacenter, storage, ring, n),
          name  = StorageNodeActor.name(nodeId, datacenter)
        )
      }

      // AKKA-MANAGEMENT
      ClusterHttpManagement(cluster).start().map { _ =>
        logger.info("Cluster HTTP-Management is ready!")
      }.recover { case ex => processOrchestrator.failure(ex) }

      // HTTP API
      val routes = logRequestResult(actorSystem.name) {
        new HttpRouter(new ActorRefStorageNodeClient(storageNodeActorRef)).routes ~
          new HealthCheckRouter().routes ~
          new BuildInfoRouter().routes(BuildInfo.toJson)
      }
      Http()
        .bindAndHandle(routes, justinConfig.http.interface, justinConfig.http.port)
        .map { binding => logger.info(s"HTTP server started at ${binding.localAddress}"); processOrchestrator.trySuccess(new JustinDB) }
        .recover { case ex => logger.error("Could not start HTTP server", ex); processOrchestrator.failure(ex) }
    }

    Await.result(processOrchestrator.future, 2.minutes)
  }
}
// $COVERAGE-ON$
