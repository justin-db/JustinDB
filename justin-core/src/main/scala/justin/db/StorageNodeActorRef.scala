package justin.db

import akka.actor.ActorRef

case class StorageNodeActorRef(storageNodeActor: ActorRef) extends AnyVal
