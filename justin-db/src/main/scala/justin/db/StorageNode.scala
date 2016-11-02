package justin.db

import akka.actor.Actor
import justin.db.storage.PluggableStorage

case class StorageNodeId(id: Int) extends AnyVal

class StorageNode(nodeId: StorageNodeId, storage: PluggableStorage) extends Actor {
  override def receive: Receive = ???
}
