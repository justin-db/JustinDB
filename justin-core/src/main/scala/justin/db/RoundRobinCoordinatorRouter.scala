package justin.db

import akka.actor.Props
import akka.routing.{DefaultResizer, RoundRobinPool}
import justin.db.actors.ReplicaCoordinatorActor
import justin.db.replica.{ReplicaReadCoordinator, ReplicaWriteCoordinator}

object RoundRobinCoordinatorRouter {
  def routerName: String = "CoordinatorRouter"

  private val pool = RoundRobinPool(
    nrOfInstances = 5,
    resizer = Some(DefaultResizer(lowerBound = 2, upperBound = 15))
  )

  def props(readCoordinator: ReplicaReadCoordinator, writeCoordinator: ReplicaWriteCoordinator): Props = {
    pool.props(ReplicaCoordinatorActor.props(readCoordinator, writeCoordinator))
  }
}
