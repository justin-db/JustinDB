package justin.db.storage

import java.util.UUID

import justin.consistent_hashing.Ring.RingPartitionId
import justin.db.Data
import justin.db.storage.PluggableStorageProtocol.{Ack, DataOriginality, StorageGetData, StoragePutData}

import scala.concurrent.Future

trait GetStorageProtocol {
  def get(id: UUID)(resolveOriginality: UUID => DataOriginality): Future[StorageGetData]
}

trait PutStorageProtocol {
  def put(cmd: StoragePutData)(resolveOriginality: UUID => DataOriginality): Future[Ack]
}

trait PluggableStorageProtocol extends GetStorageProtocol with PutStorageProtocol

object PluggableStorageProtocol {
  
  sealed trait StorageGetData
  object StorageGetData {
    case class Single(data: Data) extends StorageGetData
    case object None              extends StorageGetData
  }

  case class StoragePutData(data: Data) extends AnyVal

  sealed trait Ack
  case object Ack extends Ack {
    val future: Future[Ack] = Future.successful(Ack)
  }

  sealed trait DataOriginality { def ringPartitionId: RingPartitionId }
  object DataOriginality {
    case class Primary(ringPartitionId: RingPartitionId) extends DataOriginality
    case class Replica(ringPartitionId: RingPartitionId) extends DataOriginality
  }
}
