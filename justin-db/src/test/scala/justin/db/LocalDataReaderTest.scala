package justin.db

import java.util.UUID

import justin.db.StorageNodeActorProtocol.StorageNodeReadingResult
import justin.db.storage.PluggableStorage
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LocalDataReaderTest extends FlatSpec with Matchers with ScalaFutures {

  behavior of "Local Data Reading Service"

  it should "found data for existing key" in {
    // given
    val id = UUID.randomUUID()
    val service = new LocalDataReader(new PluggableStorage {
      override def get(id: UUID): Future[Option[Data]] = Future.successful(Option(Data(id, "value")))
      override def put(data: Data): Future[Unit] = ???
    })

    // when
    val result = service.apply(id)

    // then
    whenReady(result) { _ shouldBe StorageNodeReadingResult.Found(Data(id, "value")) }
  }

  it should "not found data for non-existing key" in {
    // given
    val id = UUID.randomUUID()
    val service = new LocalDataReader(new PluggableStorage {
      override def get(id: UUID): Future[Option[Data]] = Future.successful(None)
      override def put(data: Data): Future[Unit] = ???
    })

    // when
    val result = service.apply(id)

    // then
    whenReady(result) { _ shouldBe StorageNodeReadingResult.NotFound }
  }

  it should "recover failure reading" in {
    // given
    val id = UUID.randomUUID()
    val service = new LocalDataReader(new PluggableStorage {
      override def get(id: UUID): Future[Option[Data]] = Future.failed(new Exception)
      override def put(data: Data): Future[Unit] = ???
    })

    // when
    val result = service.apply(id)

    // then
    whenReady(result) { _ shouldBe StorageNodeReadingResult.FailedRead }
  }
}
