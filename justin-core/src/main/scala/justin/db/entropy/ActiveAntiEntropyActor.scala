package justin.db.entropy

import akka.actor.{Actor, ActorRef, Props}

class ActiveAntiEntropyActor extends Actor {
  import ActiveAntiEntropyProtocol._

  override def receive: Receive = {
    case Run => ???
  }
}

object ActiveAntiEntropyActor {
  def props: Props = Props(new ActiveAntiEntropyActor)
}

object ActiveAntiEntropyProtocol {
  case object Run
}

case class ActiveAntiEntropyActorRef(ref: ActorRef) extends AnyVal
