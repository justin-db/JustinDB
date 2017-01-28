package justin.db

import java.util.UUID

import justin.db.StorageNodeActorProtocol.StorageNodeReadingResult
import justin.db.storage.GetStorageProtocol
import justin.db.storage.PluggableStorageProtocol.{DataOriginality, StorageGetData}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReplicaLocalReaderTest extends FlatSpec with Matchers with ScalaFutures {

  behavior of "Replica Local Reader"

  it should "found data for existing key" in {
    // given
    val id   = UUID.randomUUID()
    val data = Data(id, "value")
    val service = new ReplicaLocalReader(new GetStorageProtocol {
      override def get(id: UUID)(resolveOriginality: (UUID) => DataOriginality): Future[StorageGetData] = {
        Future.successful(StorageGetData.Single(data))
      }
    })

    // when
    val result = service.apply(id, null)

    // then
    whenReady(result) { _ shouldBe StorageNodeReadingResult.Found(data) }
  }

  it should "not found data for non-existing key" in {
    // given
    val id = UUID.randomUUID()
    val service = new ReplicaLocalReader(new GetStorageProtocol {
      override def get(id: UUID)(resolveOriginality: (UUID) => DataOriginality): Future[StorageGetData] = {
        Future.successful(StorageGetData.None)
      }
    })

    // when
    val result = service.apply(id, null)

    // then
    whenReady(result) { _ shouldBe StorageNodeReadingResult.NotFound }
  }

  it should "found conflicted data for existing key" in {
    // given
    val id = UUID.randomUUID()
    val data1 = Data(id, "some-data-1")
    val data2 = Data(id, "some-data-2")
    val service = new ReplicaLocalReader(new GetStorageProtocol {
      override def get(id: UUID)(resolveOriginality: (UUID) => DataOriginality): Future[StorageGetData] = {
        Future.successful(StorageGetData.Conflicted(data1, data2))
      }
    })

    // when
    val result = service.apply(id, null)

    // then
    whenReady(result) { _ shouldBe StorageNodeReadingResult.Conflicted(data1, data2) }
  }

  it should "recover failure reading" in {
    // given
    val id = UUID.randomUUID()
    val service = new ReplicaLocalReader(new GetStorageProtocol {
      override def get(id: UUID)(resolveOriginality: (UUID) => DataOriginality): Future[StorageGetData] = Future.failed(new Exception)
    })

    // when
    val result = service.apply(id, null)

    // then
    whenReady(result) { _ shouldBe StorageNodeReadingResult.FailedRead }
  }
}
