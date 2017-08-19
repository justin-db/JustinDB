package justin.db.storage

import java.util.UUID

import justin.db.storage.PluggableStorageProtocol.{Ack, DataOriginality, StorageGetData, StoragePutData}

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * NOT THREAD-SAFE!
  */
class InMemStorage(implicit ec: ExecutionContext) extends PluggableStorageProtocol {

  private type MMap           = mutable.Map[RingPartitionId, Map[UUID, JustinData]]
  private var primaries: MMap = mutable.Map.empty[RingPartitionId, Map[UUID, JustinData]]
  private var replicas: MMap  = mutable.Map.empty[RingPartitionId, Map[UUID, JustinData]]

  override def get(id: UUID)(resolveOriginality: (UUID) => DataOriginality): Future[StorageGetData] = Future.successful {
    def get(mmap: MMap, partitionId: RingPartitionId) = {
      mmap.get(partitionId).fold[StorageGetData](StorageGetData.None) { _.get(id) match {
        case Some(data) => StorageGetData.Single(data)
        case None       => StorageGetData.None
      }}
    }

    resolveOriginality(id) match {
      case DataOriginality.Primary(partitionId) => get(primaries, partitionId)
      case DataOriginality.Replica(partitionId) => get(replicas, partitionId)
    }
  }

  override def put(putData: StoragePutData)(resolveOriginality: (UUID) => DataOriginality): Future[Ack] = {
    def update(mmap: MMap, partitionId: RingPartitionId, data: JustinData) = {
      mmap.get(partitionId) match {
        case Some(partitionMap) => mmap + (partitionId -> (partitionMap ++ Map(data.id -> data)))
        case None               => mmap + (partitionId -> Map(data.id -> data))
      }
    }

    resolveOriginality(putData.data.id) match {
      case DataOriginality.Primary(partitionId) => primaries = update(primaries, partitionId, putData.data)
      case DataOriginality.Replica(partitionId) => replicas  = update(replicas, partitionId, putData.data)
    }

    Ack.future
  }
}
