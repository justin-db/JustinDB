package justin.db

import justin.db.StorageNodeActorProtocol.StorageNodeWritingResult.{ConflictedWrite, FailedWrite, SuccessfulWrite}
import justin.db.replication.W
import org.scalatest.{FlatSpec, Matchers}

class ReachConsensusReplicatedWritesTest extends FlatSpec with Matchers {

  behavior of "Reach Consensus of Replicated Writes"

  it should "reach consensus when number of successful write operation is not smaller than desired" in {
    // given
    val w = W(1)
    val writes = List(SuccessfulWrite)

    // when
    val result = ReachConsensusReplicatedWrites.apply(w)(writes)

    // then
    result shouldBe SuccessfulWrite
  }

  it should "NOT reach consensus when number of successful write operation is smaller than desired" in {
    // given
    val w = W(1)
    val writes = List(ConflictedWrite)

    // when
    val result = ReachConsensusReplicatedWrites.apply(w)(writes)

    // then
    result shouldBe FailedWrite
  }
}
