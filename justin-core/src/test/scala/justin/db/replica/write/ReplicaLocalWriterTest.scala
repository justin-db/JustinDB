package justin.db.replica.write

import java.util.UUID

import justin.db.Data
import justin.db.actors.protocol.{StorageNodeConflictedWrite, StorageNodeFailedWrite, StorageNodeSuccessfulWrite}
import justin.db.consistenthashing.NodeId
import justin.db.storage.PluggableStorageProtocol.{Ack, DataOriginality, StorageGetData}
import justin.db.storage.{GetStorageProtocol, JustinData, PutStorageProtocol}
import justin.db.vectorclocks.{Counter, VectorClock}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class ReplicaLocalWriterTest extends FlatSpec with Matchers with ScalaFutures {

  behavior of "Replica Local Writer"

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(10.seconds, 50.millis)

  /**
    * -------------------
    * NONE scenarios     |
    * -------------------
    */
  it should "save successfully new data for not taken identificator" in {
    // given
    val notTakenId = UUID.randomUUID()
    val data       = Data(notTakenId, "some-value")
    val writer = new ReplicaLocalWriter(new GetStorageProtocol with PutStorageProtocol {
      override def get(id: UUID)(resolveOriginality: (UUID) => DataOriginality): Future[StorageGetData] = Future.successful(StorageGetData.None)
      override def put(data: JustinData)(resolveOriginality: (UUID) => DataOriginality): Future[Ack] = Ack.future
    })

    // when
    val result = writer.apply(data, null)

    // then
    whenReady(result) { _ shouldBe StorageNodeSuccessfulWrite(notTakenId) }
  }

  /**
    * -------------------
    * FAILURE scenarios  |
    * -------------------
    */
  it should "recover failure situation" in {
    // given
    val notTakenId = UUID.randomUUID()
    val data       = Data(notTakenId, "some-value")
    val writer = new ReplicaLocalWriter(new GetStorageProtocol with PutStorageProtocol {
      override def get(id: UUID)(resolveOriginality: (UUID) => DataOriginality): Future[StorageGetData] = Future.successful(StorageGetData.None)
      override def put(data: JustinData)(resolveOriginality: (UUID) => DataOriginality): Future[Ack] = Future.failed(new Exception)
    })

    // when
    val result = writer.apply(data, null)

    // then
    whenReady(result) { _ shouldBe StorageNodeFailedWrite(notTakenId) }
  }

  /**
    * -------------------
    * SINGLE scenarios   |
    * -------------------
    */
  it should "fail to write predecessor to already stored data" in {
    // given
    val id      = UUID.randomUUID()
    val data    = Data(id, "some-value", VectorClock(Map(NodeId(1) -> Counter(2))))
    val newData = Data(id, "some-value-2", VectorClock(Map(NodeId(1) -> Counter(1))))
    val writer = new ReplicaLocalWriter(new GetStorageProtocol with PutStorageProtocol {
      override def get(id: UUID)(resolveOriginality: (UUID) => DataOriginality): Future[StorageGetData] = Future.successful(StorageGetData.Single(data))
      override def put(data: JustinData)(resolveOriginality: (UUID) => DataOriginality): Future[Ack] = ???
    })

    // when
    val result = writer.apply(newData, null)

    // then
    whenReady(result) { _ shouldBe StorageNodeFailedWrite(id) }
  }

  it should "get conflicted write when trying to save new data with conflicted vector clock comparing to already existed one" in {
    // given
    val id      = UUID.randomUUID()
    val data    = Data(id, "some-value", VectorClock(Map(NodeId(1) -> Counter(1))))
    val newData = Data(id, "some-value-2", VectorClock(Map(NodeId(2) -> Counter(1))))
    val writer = new ReplicaLocalWriter(new GetStorageProtocol with PutStorageProtocol {
      override def get(id: UUID)(resolveOriginality: (UUID) => DataOriginality): Future[StorageGetData] = Future.successful(StorageGetData.Single(data))
      override def put(data: JustinData)(resolveOriginality: (UUID) => DataOriginality): Future[Ack] = Ack.future
    })

    // when
    val result = writer.apply(newData, null)

    // then
    whenReady(result) { _ shouldBe StorageNodeConflictedWrite(data, newData) }
  }

  it should "get successful write when trying to save new data with consequent vector clock comparing to already existed one" in {
    // given
    val id      = UUID.randomUUID()
    val data    = Data(id, "some-value", VectorClock(Map(NodeId(1) -> Counter(1))))
    val newData = Data(id, "some-value-2", VectorClock(Map(NodeId(1) -> Counter(2))))
    val writer = new ReplicaLocalWriter(new GetStorageProtocol with PutStorageProtocol {
      override def get(id: UUID)(resolveOriginality: (UUID) => DataOriginality): Future[StorageGetData] = Future.successful(StorageGetData.Single(data))
      override def put(data: JustinData)(resolveOriginality: (UUID) => DataOriginality): Future[Ack] = Ack.future
    })

    // when
    val result = writer.apply(newData, null)

    // then
    whenReady(result) { _ shouldBe StorageNodeSuccessfulWrite(id) }
  }
}
