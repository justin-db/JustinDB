package justin.db

import java.util.UUID

import justin.consistent_hashing.NodeId
import justin.db.replica.PreferenceList
import justin.vector_clocks.{Counter, VectorClock}
import org.scalatest.{FlatSpec, Matchers}

class DataTest extends FlatSpec with Matchers {

  behavior of "Data"

  it should "update its empty inner Vector Clock based on preference list" in {
    // given
    val data           = Data(id = UUID.randomUUID(), value = "some value")
    val preferenceList = PreferenceList(NodeId(1), List(NodeId(5), NodeId(8)))

    // when
    val updatedData = Data.updateVclock(data, preferenceList)

    // then
    val expectedVclock = VectorClock[NodeId](Map(
      NodeId(1) -> Counter(1),
      NodeId(5) -> Counter(1),
      NodeId(8) -> Counter(1))
    )
    updatedData shouldBe Data(data.id, data.value, expectedVclock, updatedData.timestamp)
  }

  it should "increase vector clock's counter of repeated nodeId when updating data" in {
    // given
    val data           = Data(id = UUID.randomUUID(), value = "some value")
    val preferenceList = PreferenceList(NodeId(1), List(NodeId(1), NodeId(1)))

    // when
    val updatedData = Data.updateVclock(data, preferenceList)

    // then
    val expectedVclock = VectorClock[NodeId](Map(
      NodeId(1) -> Counter(3)
    ))
    updatedData shouldBe Data(data.id, data.value, expectedVclock, data.timestamp)
  }

  it should "increase already existed vector clock's counter when updating data" in {
    // given
    val initVClock     = VectorClock[NodeId](Map(NodeId(1) -> Counter(3)))
    val data = Data(id = UUID.randomUUID(), value = "some value", initVClock)
    val preferenceList = PreferenceList(NodeId(1), List(NodeId(5), NodeId(8)))

    // when
    val updatedData = Data.updateVclock(data, preferenceList)

    // then
    val expectedVclock = VectorClock[NodeId](Map(
      NodeId(1) -> Counter(4),
      NodeId(5) -> Counter(1),
      NodeId(8) -> Counter(1))
    )
    updatedData shouldBe Data(data.id, data.value, expectedVclock, data.timestamp)
  }
}
