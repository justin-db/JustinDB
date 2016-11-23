package justin.vector_clocks

import java.util.UUID

import org.scalatest.{FlatSpec, Matchers}

class VectorClockTest extends FlatSpec with Matchers {

  behavior of "Vector Clock"

  it should "initialize an empty Vector Clock with passed id" in {
    val id = UUID.randomUUID()

    val vc = VectorClock.empty(id)

    vc shouldBe VectorClock(Map(VectorId(id) -> Counter(0)))
  }

  it should "increase corresponding counter by one for particular id" in {
    val id = UUID.randomUUID()
    val vc = VectorClock.empty(id)

    val increased = vc.increase(VectorId(id))

    increased shouldBe VectorClock(Map(VectorId(id) -> Counter(1)))
  }

  it should "merge two vector clocks" in {
    val id1 = VectorId(UUID.randomUUID())
    val vc1 = VectorClock(Map(id1 -> Counter(109)))

    val id2 = VectorId(UUID.randomUUID())
    val vc2 = VectorClock(Map(
      id1 -> Counter(1),
      id2 -> Counter(99)
    ))

    val receiverId = id1

    val merged = VectorClock.merge(receiverId, vc1, vc2)

    merged.byKey(id1).get shouldBe Counter(110)
    merged.byKey(id2).get shouldBe Counter(99)
  }
}
