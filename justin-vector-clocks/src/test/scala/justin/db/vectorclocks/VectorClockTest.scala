package justin.db.vectorclocks

import java.util.UUID

import org.scalatest.{FlatSpec, Matchers}

class VectorClockTest extends FlatSpec with Matchers {

  behavior of "Vector Clock"

  it should "initialize an empty Vector Clock with passed id" in {
    val id = UUID.randomUUID()

    val vc = VectorClock.empty(id)

    vc shouldBe VectorClock(Map(id -> Counter(0)))
  }

  it should "increase corresponding counter by one for particular id" in {
    val id = UUID.randomUUID()
    val vc = VectorClock.empty(id)

    val increased = vc.increase(id)

    increased shouldBe VectorClock(Map(id -> Counter(1)))
  }

  it should "merge two vector clocks" in {
    val id1 = UUID.randomUUID()
    val vc1 = VectorClock(Map(id1 -> Counter(109)))

    val id2 = UUID.randomUUID()
    val vc2 = VectorClock(Map(
      id1 -> Counter(1),
      id2 -> Counter(99)
    ))

    val receiverId = id1

    val merged = VectorClock.merge(receiverId, vc1, vc2)

    merged.get(id1).get shouldBe Counter(110)
    merged.get(id2).get shouldBe Counter(99)
  }

  it should "merge two vector clocks without having passed \"receiverId\" key" in {
    val id1 = UUID.randomUUID()
    val vc1 = VectorClock.empty(id1)

    val id2 = UUID.randomUUID()
    val vc2 = VectorClock.empty(id2)

    val receiverId = UUID.randomUUID()

    val merged = VectorClock.merge(receiverId, vc1, vc2)

    merged.get(id1).get        shouldBe Counter(0)
    merged.get(id2).get        shouldBe Counter(0)
    merged.get(receiverId).get shouldBe Counter(1)
  }

  it should "init an empty Vector Clock" in {
    type Id = Int
    val vc = VectorClock.apply[Id]()

    vc shouldBe VectorClock(Map.empty[Id, Counter])
  }

  it should "list Vector Clock" in {
    val vc = VectorClock(Map(1 -> Counter(109)))

    val list = vc.toList

    list shouldBe List((1, Counter(109)))
  }

  it should "get keys" in {
    val vc = VectorClock(Map(1 -> Counter(109), 2 -> Counter(1)))

    val keys = vc.keys

    keys shouldBe Set(1,2)
  }
}
