package justin.db

import java.util.UUID

import justin.consistent_hashing.NodeId
import justin.vector_clocks.{Counter, VectorClock}
import org.scalatest.{FlatSpec, Matchers}

class DataTest extends FlatSpec with Matchers {

  behavior of "Data"

  it should "update its empty inner Vector Clock based on preference list" in {
    // given
    val preferenceList = List(NodeId(1), NodeId(5), NodeId(8))
    val data = Data(id = UUID.randomUUID(), value = "some value")

    // when
    val updatedData = Data.updateVclock(data, preferenceList)

    // then
    val expectedVclock = VectorClock[NodeId](Map(
      NodeId(1) -> Counter(1),
      NodeId(5) -> Counter(1),
      NodeId(8) -> Counter(1))
    )
    updatedData shouldBe Data(data.id, data.value, expectedVclock)
  }
}
