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
}
