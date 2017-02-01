package justin.db

import java.util.UUID

import justin.db.ConsensusReplicatedReads.ConsensusSummary
import justin.db.StorageNodeActorProtocol.StorageNodeReadingResult.{FailedRead, Found, NotFound}
import justin.db.replication.R
import org.scalatest.{FlatSpec, Matchers}
import justin.db.versioning.VectorClockOps._

class ConsensusReplicatedReadsTest extends FlatSpec with Matchers {

  behavior of "Reach Consensus of Replicated Reads"

  it should "agreed on \"AllNotFound\" when all searched data couldn't be found" in {
    // given
    val r = 1
    val searchedData = List(NotFound, NotFound)

    // when
    val madeConsensus = new ConsensusReplicatedReads().reach(R(r))(searchedData)

    // then
    madeConsensus shouldBe ConsensusSummary.AllNotFound
  }

  it should "agreed on \"AllFailed\" when all operations during search failed" in {
    // given
    val r = 1
    val searchedData = List(FailedRead, FailedRead)

    // when
    val madeConsensus = new ConsensusReplicatedReads().reach(R(r))(searchedData)

    // then
    madeConsensus shouldBe ConsensusSummary.AllFailed
  }

  it should "agreed on \"NotEnoughFound\" when number of found replica is smaller that what client expects" in {
    // given
    val r = 2
    val searchedData = List(NotFound, FailedRead, Found(Data(UUID.randomUUID(), "value")))

    // when
    val madeConsensus = new ConsensusReplicatedReads().reach(R(r))(searchedData)

    // then
    madeConsensus shouldBe ConsensusSummary.NotEnoughFound
  }

  it should "agreed on \"Consequent\" when exactly once data is found and client expects only one replica" in {
    // given
    val r = 1
    val foundData = Found(Data(UUID.randomUUID(), "value"))
    val searchedData = List(NotFound, FailedRead, foundData)

    // when
    val madeConsensus = new ConsensusReplicatedReads().reach(R(r))(searchedData)

    // then
    madeConsensus shouldBe ConsensusSummary.Consequent(foundData.data)
  }

  it should "agreed on \"Consequent\" scenario when client expectation is achieved and consequent data could be computed" in {
    // given
    val r = 3
    val searchedData = List(
      Found(Data(UUID.randomUUID(), "value-1", "1:1")),
      Found(Data(UUID.randomUUID(), "value-2", "1:2")),
      Found(Data(UUID.randomUUID(), "value-3", "1:3"))
    )

    // when
    val madeConsensus = new ConsensusReplicatedReads().reach(R(r))(searchedData)

    // then
    madeConsensus shouldBe ConsensusSummary.Consequent(searchedData.last.data)
  }

  it should "agreed on \"Conflict\" scenario when client expectation is achieved but consequent data could be NOT computed" in {
    // given
    val r = 3
    val searchedData = List(
      Found(Data(UUID.randomUUID(), "value-1", "1:1")),
      Found(Data(UUID.randomUUID(), "value-2", "1:2")),
      Found(Data(UUID.randomUUID(), "value-3", "2:1"))
    )

    // when
    val madeConsensus = new ConsensusReplicatedReads().reach(R(r))(searchedData)

    // then
    madeConsensus shouldBe ConsensusSummary.Conflicts(searchedData.map(_.data))
  }
}
