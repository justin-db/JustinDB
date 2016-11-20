package justin.db

import java.util.UUID

import justin.db.StorageNodeActorProtocol.StorageNodeReadingResult
import justin.db.storage.PluggableStorage
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LocalDataReadingServiceTest extends FlatSpec with Matchers with ScalaFutures {

  behavior of "Local Data Reading Service"

  it should "found data for existing key" in {
    // given
    val service = new LocalDataReadingService(new PluggableStorage {
      override def get(key: String): Future[Option[String]] = Future.successful(Option("value"))
      override def put(key: String, value: String): Future[Unit] = ???
    })
    val id = UUID.randomUUID()

    // when
    val result = service.apply(id)

    // then
    whenReady(result) { _ shouldBe StorageNodeReadingResult.Found(Data(id, "value")) }
  }

  it should "not found data for non-existing key" in {
    // given
    val service = new LocalDataReadingService(new PluggableStorage {
      override def get(key: String): Future[Option[String]] = Future.successful(None)
      override def put(key: String, value: String): Future[Unit] = ???
    })
    val id = UUID.randomUUID()

    // when
    val result = service.apply(id)

    // then
    whenReady(result) { _ shouldBe StorageNodeReadingResult.NotFound }
  }

  it should "recover failure reading" in {
    // given
    val service = new LocalDataReadingService(new PluggableStorage {
      override def get(key: String): Future[Option[String]] = Future.failed(new Exception)
      override def put(key: String, value: String): Future[Unit] = ???
    })
    val id = UUID.randomUUID()

    // when
    val result = service.apply(id)

    // then
    whenReady(result) { _ shouldBe StorageNodeReadingResult.FailedRead }
  }
}
