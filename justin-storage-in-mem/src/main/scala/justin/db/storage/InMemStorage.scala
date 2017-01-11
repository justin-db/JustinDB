package justin.db.storage

import java.util.UUID

import justin.consistent_hashing.Ring.RingPartitionId
import justin.db.Data
import justin.db.storage.PluggableStorageProtocol.{Ack, DataOriginality, StorageGetData, StoragePutData}

import scala.concurrent.{ExecutionContext, Future}

/**
  * NOT THREAD-SAFE!
  */
class InMemStorage(implicit ec: ExecutionContext) extends PluggableStorageProtocol {
  import scala.collection.mutable

  private case class MapVal(data1: Data, data2: Option[Data])

  private type MMap           = mutable.Map[RingPartitionId, Map[UUID, MapVal]]
  private var primaries: MMap = mutable.Map.empty[RingPartitionId, Map[UUID, MapVal]]
  private var replicas: MMap  = mutable.Map.empty[RingPartitionId, Map[UUID, MapVal]]

  override def get(id: UUID)(resolveOriginality: (UUID) => DataOriginality): Future[StorageGetData] = Future.successful {
    def get(mmap: MMap, partitionId: RingPartitionId, id: UUID) = {
      mmap.get(partitionId).fold[StorageGetData](StorageGetData.None) { _.get(id) match {
        case None                             => StorageGetData.None
        case Some(MapVal(data1, Some(data2))) => StorageGetData.Conflicted(data1, data2)
        case Some(MapVal(data1, None))        => StorageGetData.Single(data1)
      }}
    }

    resolveOriginality(id) match {
      case DataOriginality.Primary(partitionId) => get(primaries, partitionId, id)
      case DataOriginality.Replica(partitionId) => get(replicas, partitionId, id)
    }
  }

  // TODO: handle resolving of originality of Data
  override def put(cmd: StoragePutData): Future[Ack] = {
    Ack.future
  }
}
