package justin.db.storage

import java.util.UUID

import justin.db.Data
import justin.db.storage.PluggableStorageProtocol.{Ack, DataOriginality, StorageGetData, StoragePutData}
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import language.postfixOps

class InMemStorageTest extends FlatSpec with Matchers {

  behavior of "In-memory storage"

  it should "store single data" in {
    // given
    val data = StoragePutData.Single(Data(id = UUID.randomUUID(), "some-data"))
    val inMemStorage = new InMemStorage
    val resolver = (id: UUID) => DataOriginality.Primary(ringPartitionId = 1)

    // when
    val result = Await.result(inMemStorage.put(data)(resolver), atMost = 5 seconds)

    // then
    result shouldBe Ack
  }

  it should "store conflicted data" in {
    // given
    val id = UUID.randomUUID()
    val data = StoragePutData.Conflict(id, Data(id, "some-data-1"), Data(id, "some-data-2"))
    val inMemStorage = new InMemStorage
    val resolver = (id: UUID) => DataOriginality.Primary(ringPartitionId = 1)

    // when
    val result = Await.result(inMemStorage.put(data)(resolver), atMost = 5 seconds)

    // then
    result shouldBe Ack
  }

  it should "get single data for appropriate identifier" in {
    // given
    val data = StoragePutData.Single(Data(id = UUID.randomUUID(), "some-data"))
    val inMemStorage = new InMemStorage
    val resolver = (id: UUID) => DataOriginality.Primary(ringPartitionId = 1)
    Await.result(inMemStorage.put(data)(resolver), atMost = 5 seconds)

    // when
    val result = Await.result(inMemStorage.get(data.data.id)(resolver), atMost = 5 seconds)

    // then
    result shouldBe StorageGetData.Single(data.data)
  }

  it should "get none data for not existing id in memory" in {
    // given
    val noExistingId = UUID.randomUUID()
    val inMemStorage = new InMemStorage
    val resolver = (id: UUID) => DataOriginality.Primary(ringPartitionId = 1)

    // when
    val result = Await.result(inMemStorage.get(noExistingId)(resolver), atMost = 5 seconds)

    // then
    result shouldBe StorageGetData.None
  }

  it should "get none data for not existing partitionId" in {
    // given
    val uid = UUID.randomUUID()
    val noExistingRingPartitionId = 1
    val inMemStorage = new InMemStorage
    val resolver = (id: UUID) => DataOriginality.Replica(ringPartitionId = noExistingRingPartitionId)

    // when
    val result = Await.result(inMemStorage.get(uid)(resolver), atMost = 5 seconds)

    // then
    result shouldBe StorageGetData.None
  }

  it should "get conflicted data for appropriate identifier" in {
    // given
    val id = UUID.randomUUID()
    val data = StoragePutData.Conflict(id, Data(id, "some-data-1"), Data(id, "some-data-2"))
    val inMemStorage = new InMemStorage
    val resolver = (id: UUID) => DataOriginality.Primary(ringPartitionId = 1)
    Await.result(inMemStorage.put(data)(resolver), atMost = 5 seconds)

    // when
    val result = Await.result(inMemStorage.get(id)(resolver), atMost = 5 seconds)

    // then
    result shouldBe StorageGetData.Conflicted(data.data1, data.data2)
  }

  it should "store and read replicated data" in {
    // given
    val id = UUID.randomUUID()
    val data = StoragePutData.Conflict(id, Data(id, "some-data-1"), Data(id, "some-data-2"))
    val inMemStorage = new InMemStorage
    val resolver = (id: UUID) => DataOriginality.Replica(ringPartitionId = 1)

    // when
    Await.result(inMemStorage.put(data)(resolver), atMost = 5 seconds)
    val result = Await.result(inMemStorage.get(id)(resolver), atMost = 5 seconds)

    // then
    result shouldBe StorageGetData.Conflicted(data.data1, data.data2)
  }

  it should "store and merge many data within under single partitionId" in {
    // given
    val id1 = UUID.randomUUID()
    val id2 = UUID.randomUUID()
    val data1 = StoragePutData.Single(Data(id1, "some-data"))
    val data2 = StoragePutData.Single(Data(id2, "some-data"))
    val resolver = (id: UUID) => DataOriginality.Replica(ringPartitionId = 1)
    val inMemStorage = new InMemStorage

    Await.result(inMemStorage.put(data1)(resolver), atMost = 5 seconds)
    Await.result(inMemStorage.put(data2)(resolver), atMost = 5 seconds)

    // when
    val result1 = Await.result(inMemStorage.get(id1)(resolver), atMost = 5 seconds)
    val result2 = Await.result(inMemStorage.get(id2)(resolver), atMost = 5 seconds)

    // then
    result1 shouldBe StorageGetData.Single(data1.data)
    result2 shouldBe StorageGetData.Single(data2.data)
  }
}
