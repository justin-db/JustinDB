package justin.db

import java.util.UUID

import akka.actor.Actor
import justin.db.consistent_hashing.{NodeId, Ring, UUID2RingPartitionId}
import justin.db.replication.{N, PreferenceList, W}
import justin.db.storage.PluggableStorage

class WriteNodeActor(nodeId: NodeId, storage: PluggableStorage, ring: Ring, n: N) extends Actor {

  override def receive: Receive = {
    case sv: WriteNodeActor.SaveReplicatedValue =>
      // build preference list
      val basePartitionId  = new UUID2RingPartitionId(ring).apply(sv.id)
      val preferenceList   = PreferenceList(basePartitionId, n, ring)

      // locale nodes
      val localeWrites = preferenceList
        .filter(_ == nodeId)
        .distinct

      // remote nodes
      val remoteWrites = preferenceList
        .filterNot(_ == nodeId)
        .distinct
        .flatMap(sv.clusterMembers.get)

      // replicas size
      val localReplicasSize  = localeWrites.size
      val remoteReplicasSize = remoteWrites.size
      val allReplicasSize = localReplicasSize + remoteReplicasSize

      // TODO: refactor
      if(allReplicasSize >= sv.w.w) {

        if(localReplicasSize > 0) {
          localeWrites.foreach { _ => saveLocaleValue(sv.id, sv.value) }
        }

        if(remoteReplicasSize > 0) {
          val props = WriteRemoteNodeActor.props(W(remoteReplicasSize), remoteWrites)
          val remoteActorRef = context.actorOf(props)

//          (remoteActorRef ? WriteRemoteNodeActor.SaveReplicatedValue(sv.id, sv.value))
//            .mapTo[WriteNodeActor.SuccessfulWrite]
        }

      } else {
        // not pass
      }
    case WriteNodeActor.SaveLocalValue(id, value) => saveLocaleValue(id, value); sender() ! "ack" // TODO: send more meaningful msg
  }

  private def saveLocaleValue(id: UUID, value: String) = storage.put(id.toString, value)
}

object WriteNodeActor {
  sealed trait WriteNodeActorCmd
  case class SaveReplicatedValue(id: UUID, value: String, nodeId: NodeId, clusterMembers: ClusterMembers, w: W) extends WriteNodeActorCmd
  case class SaveLocalValue(id: UUID, value: String) extends WriteNodeActorCmd
  case object SuccessfulWrite extends WriteNodeActorCmd
}
