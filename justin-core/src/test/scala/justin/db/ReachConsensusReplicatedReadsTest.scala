package justin.db

import java.util.UUID

import justin.db.StorageNodeActorProtocol.StorageNodeReadingResult.{FailedRead, Found, NotFound}
import justin.db.replication.R
import org.scalatest.{FlatSpec, Matchers}

class ReachConsensusReplicatedReadsTest extends FlatSpec with Matchers {

  behavior of "Reach Consensus of Replicated Reads"

  it should "get found data back when number of successful reads is reached" in {
    // given
    val r = R(1)
    val reads = List(Found(Data(id = UUID.randomUUID(), "value1")), FailedRead, NotFound)

    // when
    val result = ReachConsensusReplicatedReads.apply(r)(reads)

    // then
    result shouldBe reads.head
  }

  it should "get failed when all of the reads operation got failed" in {
    // given
    val r = R(1)
    val reads = List(FailedRead, FailedRead, FailedRead)

    // when
    val result = ReachConsensusReplicatedReads.apply(r)(reads)

    result shouldBe FailedRead
  }

  it should "not found asked data when number of successful reads is not reached and not all of them are failed" in {
    // given
    val r = R(2)
    val reads = List(Found(Data(id = UUID.randomUUID(), "value1")), FailedRead, NotFound)

    // when
    val result = ReachConsensusReplicatedReads.apply(r)(reads)

    // then
    result shouldBe NotFound
  }
}
