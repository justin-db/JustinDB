package justin.vector_clocks

import java.util.UUID

case class VectorClock(private val clock: Map[VectorId, Counter]) {
  def get(id: VectorId): Option[Counter] = clock.get(id)

  def increase(id: VectorId): VectorClock = {
    val searchedCounter = clock.getOrElse(id, Counter.zero)
    val updatedCounter  = searchedCounter.addOne

    VectorClock(clock + (id -> updatedCounter))
  }
}

object VectorClock {

  def apply(): VectorClock = VectorClock(Map.empty[VectorId, Counter])

  def empty(id: UUID): VectorClock = VectorClock(Map(VectorId(id) -> Counter.zero))

  def merge(receiverId: VectorId, vc1: VectorClock, vc2: VectorClock): VectorClock = {
    val mergedClocks = vc1.clock ++ vc2.clock

    val mergedCounter = (vc1.clock.get(receiverId), vc2.clock.get(receiverId)) match {
      case (Some(counter1), Some(counter2)) => Counter.max(counter1, counter2)
      case (None, Some(counter2))           => counter2
      case (Some(counter1), None)           => counter1
      case (None, None)                     => Counter.zero
    }

    val counter = mergedCounter.addOne

    VectorClock(mergedClocks + (receiverId -> counter))
  }
}

case class VectorId(uuid: UUID) extends AnyVal
