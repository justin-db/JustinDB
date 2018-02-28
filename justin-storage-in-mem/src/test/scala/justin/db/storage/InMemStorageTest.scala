package justin.db.storage

import java.util.UUID

import justin.db.storage.PluggableStorageProtocol.{Ack, StorageGetData}
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class InMemStorageTest extends FlatSpec with Matchers {

  behavior of "In-memory storage"

  it should "store single data" in {
    // given
    val data         = prepareData(UUID.randomUUID(), "some-data")
    val inMemStorage = new InMemStorage

    // when
    val result = Await.result(inMemStorage.put(data), atMost = 5 seconds)

    // then
    result shouldBe Ack
  }

  it should "get single data for appropriate identifier" in {
    // given
    val data         = prepareData(UUID.randomUUID(), "some-data")
    val inMemStorage = new InMemStorage
    Await.result(inMemStorage.put(data), atMost = 5 seconds)

    // when
    val result = Await.result(inMemStorage.get(data.id), atMost = 5 seconds)

    // then
    result shouldBe StorageGetData.Single(data)
  }

  it should "get none data for not existing id in memory" in {
    // given
    val noExistingId = UUID.randomUUID()
    val inMemStorage = new InMemStorage
    val otherData    = prepareData(id = UUID.randomUUID(), "some-data")
    Await.result(inMemStorage.put(otherData), atMost = 5 seconds)

    // when
    val result = Await.result(inMemStorage.get(noExistingId), atMost = 5 seconds)

    // then
    result shouldBe StorageGetData.None
  }

  private def prepareData(id: UUID, value: String) = JustinData(id, value, "", 1L)
}
