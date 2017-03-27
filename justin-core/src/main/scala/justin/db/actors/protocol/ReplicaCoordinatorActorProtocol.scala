package justin.db.actors.protocol

import akka.actor.ActorRef
import justin.db.ClusterMembers

case class WriteData(sender: ActorRef, clusterMembers: ClusterMembers, cmd: StorageNodeWriteData)
case class ReadData(sender: ActorRef, clusterMembers: ClusterMembers, cmd: StorageNodeReadData)
