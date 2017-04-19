package justin.db.actors.protocol

import akka.actor.ActorRef
import justin.db.cluster.ClusterMembers

case class WriteData(sender: ActorRef, clusterMembers: ClusterMembers, cmd: StorageNodeWriteRequest)
case class ReadData(sender: ActorRef, clusterMembers: ClusterMembers, cmd: StorageNodeReadRequest)
