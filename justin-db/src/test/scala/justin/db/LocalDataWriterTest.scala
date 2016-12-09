package justin.db

import java.util.UUID

import justin.db.StorageNodeActorProtocol.StorageNodeWritingResult
import justin.db.storage.PluggableStorageProtocol
import justin.db.storage.PluggableStorageProtocol.{Ack, StorageGetData, StoragePutData}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LocalDataWriterTest extends FlatSpec with Matchers with ScalaFutures {

  behavior of "Local Data Writer"

  it should "save successfully new data for not taken identificator" in {
    // given
    val notTakenId = UUID.randomUUID()
    val data = Data(notTakenId, "some-value")
    val writer = new LocalDataWriter(new PluggableStorageProtocol {
      override def get(id: UUID): Future[StorageGetData] = Future.successful(StorageGetData.None)
      override def put(cmd: StoragePutData): Future[Ack] = Ack.future
    })

    // when
    val result = writer.apply(data)

    // then
    whenReady(result) { _ shouldBe StorageNodeWritingResult.SuccessfulWrite }
  }

  it should "recover failure situation" in {
    // given
    val notTakenId = UUID.randomUUID()
    val data = Data(notTakenId, "some-value")
    val writer = new LocalDataWriter(new PluggableStorageProtocol {
      override def get(id: UUID): Future[StorageGetData] = Future.successful(StorageGetData.None)
      override def put(cmd: StoragePutData): Future[Ack] = Future.failed(new Exception)
    })

    // when
    val result = writer.apply(data)

    // then
    whenReady(result) { _ shouldBe StorageNodeWritingResult.FailedWrite }
  }
}
