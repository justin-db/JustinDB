package justin.db.replica.multidatacenter

import akka.actor.ActorPath
import justin.db.actors.StorageNodeActorRef
import justin.db.actors.protocol.MultiDataCenterContacts
import justin.db.replica.multidatacenter.InitialContactsValidator.InitialContactsResponse

import scala.util.{Failure, Success, Try}

class InitialContactsValidator(storageNodeActorRef: StorageNodeActorRef) extends (List[String] => InitialContactsResponse) {
  override def apply(initialContacts: List[String]): InitialContactsResponse = {
    Try(initialContacts.toSet.map(ActorPath.fromString)) match {
      case Success(contacts) if contacts.nonEmpty => storageNodeActorRef.ref ! MultiDataCenterContacts(contacts); InitialContactsValidator.OK
      case Success(_) => InitialContactsValidator.NotValid
      case Failure(_) => InitialContactsValidator.NotValid
    }
  }
}

object InitialContactsValidator {
  sealed trait InitialContactsResponse
  case object OK extends InitialContactsResponse
  case object NotValid extends InitialContactsResponse
}
