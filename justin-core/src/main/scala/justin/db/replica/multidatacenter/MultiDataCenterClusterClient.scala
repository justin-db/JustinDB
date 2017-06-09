package justin.db.replica.multidatacenter

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.client.ClusterClient

class MultiDataCenterClusterClient(clusterClient: ActorRef, storageNodeActorName: String) extends Actor with ActorLogging {
  import MultiDataCenterClusterClient._

  def receive: Receive = {
    case msg => clusterClient ! ClusterClient.Send(receptionistPath(storageNodeActorName), msg, localAffinity = false)
  }
}

object MultiDataCenterClusterClient {
  def receptionistPath(storageNodeActorName: String): String = "/user/" + storageNodeActorName

  def props(clusterClient: ActorRef, storageNodeActorName: String): Props = Props(new MultiDataCenterClusterClient(clusterClient, storageNodeActorName))
}
