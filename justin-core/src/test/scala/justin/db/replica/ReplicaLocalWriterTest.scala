package justin.db.replica

import java.util.UUID

import justin.consistent_hashing.NodeId
import justin.db.Data
import justin.db.actors.StorageNodeActorProtocol.StorageNodeWritingResult
import justin.db.replica.ReplicaLocalWriter
import justin.db.storage.PluggableStorageProtocol.{Ack, DataOriginality, StorageGetData, StoragePutData}
import justin.db.storage.{GetStorageProtocol, PutStorageProtocol}
import justin.vector_clocks.{Counter, VectorClock}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class ReplicaLocalWriterTest extends FlatSpec with Matchers with ScalaFutures {

  behavior of "Replica Local Writer"

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
      override def get(id: UUID)(resolveOriginality: (UUID) => DataOriginality)(implicit ec: ExecutionContext): Future[StorageGetData] = Future.successful(StorageGetData.None)
      override def put(cmd: StoragePutData)(resolveOriginality: (UUID) => DataOriginality)(implicit ec: ExecutionContext): Future[Ack] = Ack.future
    })

    // when
    val result = writer.apply(data, null)

    // then
    whenReady(result) { _ shouldBe StorageNodeWritingResult.SuccessfulWrite }
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
      override def get(id: UUID)(resolveOriginality: (UUID) => DataOriginality)(implicit ec: ExecutionContext): Future[StorageGetData] = Future.successful(StorageGetData.None)
      override def put(cmd: StoragePutData)(resolveOriginality: (UUID) => DataOriginality)(implicit ec: ExecutionContext): Future[Ack] = Future.failed(new Exception)
    })

    // when
    val result = writer.apply(data, null)

    // then
    whenReady(result) { _ shouldBe StorageNodeWritingResult.FailedWrite }
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
      override def get(id: UUID)(resolveOriginality: (UUID) => DataOriginality)(implicit ec: ExecutionContext): Future[StorageGetData] = Future.successful(StorageGetData.Single(data))
      override def put(cmd: StoragePutData)(resolveOriginality: (UUID) => DataOriginality)(implicit ec: ExecutionContext): Future[Ack] = ???
    })

    // when
    val result = writer.apply(newData, null)

    // then
    whenReady(result) { _ shouldBe StorageNodeWritingResult.FailedWrite }
  }

  it should "get conflicted write when trying to save new data with conflicted vector clock comparing to already existed one" in {
    // given
    val id      = UUID.randomUUID()
    val data    = Data(id, "some-value", VectorClock(Map(NodeId(1) -> Counter(1))))
    val newData = Data(id, "some-value-2", VectorClock(Map(NodeId(2) -> Counter(1))))
    val writer = new ReplicaLocalWriter(new GetStorageProtocol with PutStorageProtocol {
      override def get(id: UUID)(resolveOriginality: (UUID) => DataOriginality)(implicit ec: ExecutionContext): Future[StorageGetData] = Future.successful(StorageGetData.Single(data))
      override def put(cmd: StoragePutData)(resolveOriginality: (UUID) => DataOriginality)(implicit ec: ExecutionContext): Future[Ack] = Ack.future
    })

    // when
    val result = writer.apply(newData, null)

    // then
    whenReady(result) { _ shouldBe StorageNodeWritingResult.ConflictedWrite(data, newData) }
  }

  it should "get successful write when trying to save new data with consequent vector clock comparing to already existed one" in {
    // given
    val id      = UUID.randomUUID()
    val data    = Data(id, "some-value", VectorClock(Map(NodeId(1) -> Counter(1))))
    val newData = Data(id, "some-value-2", VectorClock(Map(NodeId(1) -> Counter(2))))
    val writer = new ReplicaLocalWriter(new GetStorageProtocol with PutStorageProtocol {
      override def get(id: UUID)(resolveOriginality: (UUID) => DataOriginality)(implicit ec: ExecutionContext): Future[StorageGetData] = Future.successful(StorageGetData.Single(data))
      override def put(cmd: StoragePutData)(resolveOriginality: (UUID) => DataOriginality)(implicit ec: ExecutionContext): Future[Ack] = Ack.future
    })

    // when
    val result = writer.apply(newData, null)

    // then
    whenReady(result) { _ shouldBe StorageNodeWritingResult.SuccessfulWrite }
  }
}
