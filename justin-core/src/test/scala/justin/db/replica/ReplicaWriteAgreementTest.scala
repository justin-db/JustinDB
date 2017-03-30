package justin.db.replica

import java.util.UUID

import justin.db.actors.protocol.StorageNodeSuccessfulWrite
import justin.db.actors.protocol.StorageNodeWritingResult.StorageNodeFailedWrite
import justin.db.replica.ReplicaWriteAgreement.WriteAgreement
import org.scalatest.{FlatSpec, Matchers}

class ReplicaWriteAgreementTest extends FlatSpec with Matchers {

  behavior of "Reach Consensus of Replicated Writes"

  it should "agreed on \"SuccessfulWrite\" if number of successful write is not less than client expectations" in {
    // given
    val w = W(2)
    val writes = List(StorageNodeSuccessfulWrite(UUID.randomUUID()), StorageNodeSuccessfulWrite(UUID.randomUUID()), StorageNodeFailedWrite(UUID.randomUUID()))

    // when
    val result = new ReplicaWriteAgreement().reach(w)(writes)

    // then
    result shouldBe WriteAgreement.Ok
  }

  it should "agreed on \"NotEnoughWrites\" if number of successful write is less than client expectations" in {
    // given
    val w = W(3)
    val writes = List(StorageNodeSuccessfulWrite(UUID.randomUUID()), StorageNodeSuccessfulWrite(UUID.randomUUID()), StorageNodeFailedWrite(UUID.randomUUID()))

    // when
    val result = new ReplicaWriteAgreement().reach(w)(writes)

    // then
    result shouldBe WriteAgreement.NotEnoughWrites
  }
}
