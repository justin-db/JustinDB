package justin.vector_clocks

import java.util.UUID

case class VectorId(uuid: UUID) extends AnyVal
case class Counter(value: Int) extends AnyVal {
  def addOne: Counter = this.copy(value = value + 1)
}

object Counter {
  def max(c1: Counter, c2: Counter): Counter = {
    Counter(scala.math.max(c1.value, c2.value))
  }
}

case class VectorClock(private val clock: Map[VectorId, Counter])

object VectorClock {

  def empty(id: UUID): VectorClock = VectorClock(Map(VectorId(id) -> Counter(0)))

  def increase(vc: VectorClock, id: VectorId): VectorClock = {
    val searchedCounter = vc.clock.getOrElse(id, Counter(0))
    val updatedCounter = searchedCounter.addOne

    VectorClock(vc.clock + (id -> updatedCounter))
  }

  def merge(receiverId: VectorId, vc1: VectorClock, vc2: VectorClock): VectorClock = {
    val mergedClocks = vc1.clock ++ vc2.clock

    val mergedCounter = (vc1.clock.get(receiverId), vc2.clock.get(receiverId)) match {
      case (Some(counter1), Some(counter2)) => Counter.max(counter1, counter2)
      case (None, Some(counter2))           => counter2
      case (Some(counter1), None)           => counter1
      case (None, None)                     => Counter(0)
    }

    val counter = mergedCounter.addOne

    VectorClock(mergedClocks + (receiverId -> counter))
  }
}
