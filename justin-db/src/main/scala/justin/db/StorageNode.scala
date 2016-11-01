package justin.db

import akka.actor.Actor
import justin.db.storage.PluggableStorage

class StorageNode(storage: PluggableStorage) extends Actor {
  override def receive: Receive = ???
}
