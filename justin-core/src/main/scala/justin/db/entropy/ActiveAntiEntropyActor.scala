package justin.db.entropy

import akka.actor.{Actor, ActorRef, Props}

class ActiveAntiEntropyActor extends Actor {
  import ActiveAntiEntropyProtocol._

  override def receive: Receive = {
    case Run => ??? // TODO: start building of Merkle Tree on top of data
  }
}

object ActiveAntiEntropyActor {
  def props: Props = Props(new ActiveAntiEntropyActor)
}

object ActiveAntiEntropyProtocol {
  case object Run
}

case class ActiveAntiEntropyActorRef(ref: ActorRef) extends AnyVal
