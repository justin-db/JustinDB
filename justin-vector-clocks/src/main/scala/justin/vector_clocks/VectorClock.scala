package justin.vector_clocks

import java.util.UUID

case class VectorId(uuid: UUID) extends AnyVal
case class Counter(value: Int) extends AnyVal

case class VectorClock(private val clock: Map[VectorId, Counter])

object VectorClock {

  def empty(id: UUID): VectorClock = VectorClock(Map(VectorId(id) -> Counter(0)))

  def increase(vc: VectorClock, id: VectorId): Option[VectorClock] = {
    for {
      keyCounter <- vc.clock.get(id)
      updatedCounter = Counter(keyCounter.value + 1)
    } yield {
      VectorClock(vc.clock + (id -> updatedCounter))
    }
  }

  def merge(receiverId: VectorId, vc1: VectorClock, vc2: VectorClock): VectorClock = {

    val mergedClocks = vc1.clock ++ vc2.clock

    val mergedCounter = (vc1.clock.get(receiverId), vc2.clock.get(receiverId)) match {
      case (Some(counter1), Some(counter2)) => scala.math.max(counter1.value, counter2.value)
      case (None, Some(counter2))           => counter2.value
      case (Some(counter1), None)           => counter1.value
      case (None, None)                     => 0
    }

    val counter = Counter(mergedCounter + 1)

    VectorClock(mergedClocks + (receiverId -> counter))
  }
}
