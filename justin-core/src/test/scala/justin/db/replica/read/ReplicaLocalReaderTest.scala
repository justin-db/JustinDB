package justin.db.replica.read

import java.util.UUID

import justin.db.Data
import justin.db.actors.protocol.{StorageNodeFailedRead, StorageNodeFoundRead, StorageNodeNotFoundRead}
import justin.db.consistenthashing.NodeId
import justin.db.storage.GetStorageProtocol
import justin.db.storage.PluggableStorageProtocol.StorageGetData
import justin.db.vectorclocks.VectorClock
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class ReplicaLocalReaderTest extends FlatSpec with Matchers with ScalaFutures {

  behavior of "Replica Local Reader"

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(10.seconds, 50.millis)

  it should "found data for existing key" in {
    // given
    val id   = UUID.randomUUID()
    val data = Data(id, "value", VectorClock[NodeId]().increase(NodeId(1)))
    val service = new ReplicaLocalReader(new GetStorageProtocol {
      override def get(id: UUID): Future[StorageGetData] = {
        Future.successful(StorageGetData.Single(data))
      }
    })

    // when
    val result = service.apply(id)

    // then
    whenReady(result) { _ shouldBe StorageNodeFoundRead(data) }
  }

  it should "not found data for non-existing key" in {
    // given
    val id = UUID.randomUUID()
    val service = new ReplicaLocalReader(new GetStorageProtocol {
      override def get(id: UUID): Future[StorageGetData] = {
        Future.successful(StorageGetData.None)
      }
    })

    // when
    val result = service.apply(id)

    // then
    whenReady(result) { _ shouldBe StorageNodeNotFoundRead(id) }
  }

  it should "recover failure reading" in {
    // given
    val id = UUID.randomUUID()
    val service = new ReplicaLocalReader(new GetStorageProtocol {
      override def get(id: UUID): Future[StorageGetData] = Future.failed(new Exception)
    })

    // when
    val result = service.apply(id)

    // then
    whenReady(result) { _ shouldBe StorageNodeFailedRead(id) }
  }
}
