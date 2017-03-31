package justin.db.replica

import java.util.UUID

import justin.db.Data
import justin.db.replica.ReplicaReadAgreement.ReadAgreement
import justin.db.actors.protocol.StorageNodeReadResponse.{FailedRead, StorageNodeFoundRead, NotFound}
import org.scalatest.{FlatSpec, Matchers}
import justin.db.versioning.VectorClockOps._
import org.scalatest.{FlatSpec, Matchers}

class ReplicaReadAgreementTest extends FlatSpec with Matchers {

  behavior of "Reach Consensus of Replicated Reads"

  it should "agreed on \"AllNotFound\" when all searched data couldn't be found" in {
    // given
    val r = 1
    val searchedData = List(NotFound, NotFound)

    // when
    val madeConsensus = new ReplicaReadAgreement().reach(R(r))(searchedData)

    // then
    madeConsensus shouldBe ReadAgreement.AllNotFound
  }

  it should "agreed on \"AllFailed\" when all operations during search failed" in {
    // given
    val r = 1
    val searchedData = List(FailedRead, FailedRead)

    // when
    val madeConsensus = new ReplicaReadAgreement().reach(R(r))(searchedData)

    // then
    madeConsensus shouldBe ReadAgreement.AllFailed
  }

  it should "agreed on \"NotEnoughFound\" when number of found replica is smaller that what client expects" in {
    // given
    val r = 2
    val searchedData = List(NotFound, FailedRead, StorageNodeFoundRead(Data(UUID.randomUUID(), "value")))

    // when
    val madeConsensus = new ReplicaReadAgreement().reach(R(r))(searchedData)

    // then
    madeConsensus shouldBe ReadAgreement.NotEnoughFound
  }

  it should "agreed on \"Consequent\" scenario when client expectation is achieved and consequent data could be computed" in {
    // given
    val r = 3
    val searchedData = List(
      StorageNodeFoundRead(Data(UUID.randomUUID(), "value-1", "1:1")),
      StorageNodeFoundRead(Data(UUID.randomUUID(), "value-2", "1:2")),
      StorageNodeFoundRead(Data(UUID.randomUUID(), "value-3", "1:3"))
    )

    // when
    val madeConsensus = new ReplicaReadAgreement().reach(R(r))(searchedData)

    // then
    madeConsensus shouldBe ReadAgreement.Consequent(searchedData.last.data)
  }

  it should "agreed on \"Conflict\" scenario when client expectation is achieved but consequent data could NOT be computed" in {
    // given
    val r = 3
    val searchedData = List(
      StorageNodeFoundRead(Data(UUID.randomUUID(), "value-1", "1:1")),
      StorageNodeFoundRead(Data(UUID.randomUUID(), "value-2", "1:2")),
      StorageNodeFoundRead(Data(UUID.randomUUID(), "value-3", "2:1"))
    )

    // when
    val madeConsensus = new ReplicaReadAgreement().reach(R(r))(searchedData)

    // then
    madeConsensus shouldBe ReadAgreement.Conflicts(searchedData.map(_.data))
  }

  it should "agreed on \"Found\" when exactly once data is found and client expects only one replica" in {
    // given
    val r = 1
    val foundData = StorageNodeFoundRead(Data(UUID.randomUUID(), "value"))
    val searchedData = List(NotFound, FailedRead, foundData)

    // when
    val madeConsensus = new ReplicaReadAgreement().reach(R(r))(searchedData)

    // then
    madeConsensus shouldBe ReadAgreement.Found(foundData.data)
  }

  it should "agreed on \"Found\" scenario when client expectation is achieved and all replicas agreed on same value" in {
    // given
    val r = 3
    val searchedData = List(
      StorageNodeFoundRead(Data(UUID.randomUUID(), "value-1", "2:1")),
      StorageNodeFoundRead(Data(UUID.randomUUID(), "value-1", "2:1")),
      StorageNodeFoundRead(Data(UUID.randomUUID(), "value-1", "2:1"))
    )

    // when
    val madeConsensus = new ReplicaReadAgreement().reach(R(r))(searchedData)

    // then
    madeConsensus shouldBe ReadAgreement.Found(searchedData.head.data)
  }
}
