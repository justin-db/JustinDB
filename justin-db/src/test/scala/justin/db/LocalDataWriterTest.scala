package justin.db

import java.util.UUID

import justin.db.StorageNodeActorProtocol.StorageNodeWritingResult
import justin.db.storage.PluggableStorageProtocol
import justin.db.storage.PluggableStorageProtocol.StorageGetData
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LocalDataWriterTest extends FlatSpec with Matchers with ScalaFutures {

  behavior of "Local Data Writer"

  it should "successfully write data" in {
    pending
    // given
    val service = new LocalDataWriter(new PluggableStorageProtocol {
      override def get(id: UUID): Future[StorageGetData] = ???
      override def put(data: Data): Future[Unit] = Future.successful(())
    })
    val data = Data(id = UUID.randomUUID(), value = "exemplary-value")

    // when
    val result = service.apply(data)

    // then
    whenReady(result) { _ shouldBe StorageNodeWritingResult.SuccessfulWrite }
  }

  it should "recover for failed write" in {
    pending
    // given
    val service = new LocalDataWriter(new PluggableStorageProtocol {
      override def get(id: UUID): Future[StorageGetData] = ???
      override def put(data: Data): Future[Unit] = Future.failed(new Exception)
    })
    val data = Data(id = UUID.randomUUID(), value = "exemplary-value")

    // when
    val result = service.apply(data)

    // then
    whenReady(result) { _ shouldBe StorageNodeWritingResult.FailedWrite }
  }
}
