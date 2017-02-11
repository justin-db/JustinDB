package justin.db

import justin.db.ConsensusReplicatedWrites.ConsensusSummary
import justin.db.StorageNodeActorProtocol.StorageNodeWritingResult.{FailedWrite, SuccessfulWrite}
import justin.db.replication.W
import org.scalatest.{FlatSpec, Matchers}

class ConsensusReplicatedWritesTest extends FlatSpec with Matchers {

  behavior of "Reach Consensus of Replicated Writes"

  it should "agreed on \"SuccessfulWrite\" if number of successful write is not less than client expectations" in {
    // given
    val w = W(2)
    val writes = List(SuccessfulWrite, SuccessfulWrite, FailedWrite)

    // when
    val result = new ConsensusReplicatedWrites().reach(w)(writes)

    // then
    result shouldBe ConsensusSummary.Ok
  }

  it should "agreed on \"NotEnoughWrites\" if number of successful write is less than client expectations" in {
    // given
    val w = W(3)
    val writes = List(SuccessfulWrite, SuccessfulWrite, FailedWrite)

    // when
    val result = new ConsensusReplicatedWrites().reach(w)(writes)

    // then
    result shouldBe ConsensusSummary.NotEnoughWrites
  }
}
