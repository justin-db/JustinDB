package justin.db

import java.util.UUID

import justin.consistent_hashing.NodeId
import justin.db.StorageNodeActorProtocol.StorageNodeWritingResult
import justin.db.storage.PluggableStorageProtocol
import justin.db.storage.PluggableStorageProtocol.{Ack, StorageGetData, StoragePutData}
import justin.vector_clocks.{Counter, VectorClock}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReplicaLocalWriterTest extends FlatSpec with Matchers with ScalaFutures {

  behavior of "Local Data Writer"

  /**
    * -------------------
    * NONE scenarios     |
    * -------------------
    */
  it should "save successfully new data for not taken identificator" in {
    // given
    val notTakenId = UUID.randomUUID()
    val data       = Data(notTakenId, "some-value")
    val writer = new ReplicaLocalWriter(new PluggableStorageProtocol {
      override def get(id: UUID): Future[StorageGetData] = Future.successful(StorageGetData.None)
      override def put(cmd: StoragePutData): Future[Ack] = Ack.future
    })

    // when
    val result = writer.apply(data)

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
    val writer = new ReplicaLocalWriter(new PluggableStorageProtocol {
      override def get(id: UUID): Future[StorageGetData] = Future.successful(StorageGetData.None)
      override def put(cmd: StoragePutData): Future[Ack] = Future.failed(new Exception)
    })

    // when
    val result = writer.apply(data)

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
    val writer = new ReplicaLocalWriter(new PluggableStorageProtocol {
      override def get(id: UUID): Future[StorageGetData] = Future.successful(StorageGetData.Single(data))
      override def put(cmd: StoragePutData): Future[Ack] = ???
    })

    // when
    val result = writer.apply(newData)

    // then
    whenReady(result) { _ shouldBe StorageNodeWritingResult.FailedWrite }
  }

  it should "get conflicted write when trying to save new data with conflicted vector clock comparing to already existed one" in {
    // given
    val id      = UUID.randomUUID()
    val data    = Data(id, "some-value", VectorClock(Map(NodeId(1) -> Counter(1))))
    val newData = Data(id, "some-value-2", VectorClock(Map(NodeId(2) -> Counter(1))))
    val writer = new ReplicaLocalWriter(new PluggableStorageProtocol {
      override def get(id: UUID): Future[StorageGetData] = Future.successful(StorageGetData.Single(data))
      override def put(cmd: StoragePutData): Future[Ack] = Ack.future
    })

    // when
    val result = writer.apply(newData)

    // then
    whenReady(result) { _ shouldBe StorageNodeWritingResult.ConflictedWrite }
  }

  it should "get successful write when trying to save new data with consequent vector clock comparing to already existed one" in {
    // given
    val id      = UUID.randomUUID()
    val data    = Data(id, "some-value", VectorClock(Map(NodeId(1) -> Counter(1))))
    val newData = Data(id, "some-value-2", VectorClock(Map(NodeId(1) -> Counter(2))))
    val writer = new ReplicaLocalWriter(new PluggableStorageProtocol {
      override def get(id: UUID): Future[StorageGetData] = Future.successful(StorageGetData.Single(data))
      override def put(cmd: StoragePutData): Future[Ack] = Ack.future
    })

    // when
    val result = writer.apply(newData)

    // then
    whenReady(result) { _ shouldBe StorageNodeWritingResult.SuccessfulWrite }
  }

  /**
    * ---------------------
    * CONFLICTED scenarios |
    * ---------------------
    */
    it should "get successful write when trying to save new data with consequent vector clock to both conflicted data" in {
      // given
      val id             = UUID.randomUUID()
      val existedData1   = Data(id, "some-value", VectorClock(Map(NodeId(2) -> Counter(1))))
      val existedData2   = Data(id, "some-value", VectorClock(Map(NodeId(1) -> Counter(1))))
      val consequentData = Data(id, "some-value", VectorClock(Map(NodeId(1) -> Counter(1), NodeId(2) -> Counter(1))))
      val writer = new ReplicaLocalWriter(new PluggableStorageProtocol {
        override def get(id: UUID): Future[StorageGetData] = Future.successful(StorageGetData.Conflicted(existedData1, existedData2))
        override def put(cmd: StoragePutData): Future[Ack] = Ack.future
      })

      // when
      val result = writer.apply(consequentData)

      // then
      whenReady(result) { _ shouldBe StorageNodeWritingResult.SuccessfulWrite }
    }

    it should "get failed write if new data has not consequent vector clock comparing to every conflicted data" in {
      // given
      val id             = UUID.randomUUID()
      val existedData1   = Data(id, "some-value", VectorClock(Map(NodeId(2) -> Counter(1))))
      val existedData2   = Data(id, "some-value", VectorClock(Map(NodeId(3) -> Counter(1))))
      val consequentData = Data(id, "some-value", VectorClock(Map(NodeId(1) -> Counter(1), NodeId(2) -> Counter(1))))
      val writer = new ReplicaLocalWriter(new PluggableStorageProtocol {
        override def get(id: UUID): Future[StorageGetData] = Future.successful(StorageGetData.Conflicted(existedData1, existedData2))
        override def put(cmd: StoragePutData): Future[Ack] = ???
      })

      // when
      val result = writer.apply(consequentData)

      // then
      whenReady(result) { _ shouldBe StorageNodeWritingResult.FailedWrite }
    }
}
