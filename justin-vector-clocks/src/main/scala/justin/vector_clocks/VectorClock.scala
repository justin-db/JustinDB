package justin.vector_clocks

case class VectorClock[Id](private val clock: Map[Id, Counter]) {
  def get(id: Id): Option[Counter] = clock.get(id)

  def increase(id: Id): VectorClock[Id] = {
    val searchedCounter = clock.getOrElse(id, Counter.zero)
    val updatedCounter  = searchedCounter.addOne

    VectorClock(clock + (id -> updatedCounter))
  }

  def toList: List[(Id, Counter)] = clock.toList

  def keys: Set[Id] = clock.keys.toSet
}

object VectorClock {

  def apply[Id](): VectorClock[Id] = VectorClock(Map.empty[Id, Counter])

  def empty[Id](id: Id): VectorClock[Id] = VectorClock(Map(id -> Counter.zero))

  def merge[Id](receiverId: Id, vc1: VectorClock[Id], vc2: VectorClock[Id]): VectorClock[Id] = {
    val mergedClocks = vc1.clock ++ vc2.clock

    val mergedCounter = (vc1.clock.get(receiverId), vc2.clock.get(receiverId)) match {
      case (Some(counter1), Some(counter2)) => Counter.max(counter1, counter2)
      case (None, Some(counter2)) => counter2
      case (Some(counter1), None) => counter1
      case (None, None) => Counter.zero
    }

    val counter = mergedCounter.addOne

    VectorClock(mergedClocks + (receiverId -> counter))
  }
}
