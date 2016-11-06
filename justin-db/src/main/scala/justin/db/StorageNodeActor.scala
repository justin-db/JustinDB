package justin.db

import java.util.UUID

import akka.actor.{Actor, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.MemberUp
import justin.consistent_hashing.Ring
import justin.db.StorageNodeActor.{GetValue, PutValue}
import justin.db.storage.PluggableStorage

case class StorageNodeActorId(id: Int) extends AnyVal

class StorageNodeActor(nodeId: StorageNodeActorId, storage: PluggableStorage, ring: Ring) extends Actor {

  val cluster = Cluster(context.system)

  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(self)

  override def receive: Receive = {
    case GetValue(id)        => sender() ! storage.get(id.toString)
    case PutValue(id, value) => storage.put(id.toString, value); sender() ! "ack" // TODO: send back more meaningful value
    case t                   => println("msg: " + t)
  }
}

object StorageNodeActor {

  sealed trait StorageNodeReq
  case class GetValue(id: UUID) extends StorageNodeReq
  case class PutValue(id: UUID, value: String) extends StorageNodeReq

  def role: String = "StorageNode"

  def name(nodeId: StorageNodeActorId): String = s"id-${nodeId.id}"

  def props(nodeId: StorageNodeActorId, storage: PluggableStorage, ring: Ring): Props = {
    Props(new StorageNodeActor(nodeId, storage, ring))
  }
}
