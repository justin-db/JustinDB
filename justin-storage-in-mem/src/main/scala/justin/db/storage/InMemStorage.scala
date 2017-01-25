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
    def get(mmap: MMap, partitionId: RingPartitionId) = {
      mmap.get(partitionId).fold[StorageGetData](StorageGetData.None) { _.get(id) match {
        case None                             => StorageGetData.None
        case Some(MapVal(data1, Some(data2))) => StorageGetData.Conflicted(data1, data2)
        case Some(MapVal(data1, None))        => StorageGetData.Single(data1)
      }}
    }

    resolveOriginality(id) match {
      case DataOriginality.Primary(partitionId) => get(primaries, partitionId)
      case DataOriginality.Replica(partitionId) => get(replicas, partitionId)
    }
  }

  override def put(cmd: StoragePutData)(resolveOriginality: (UUID) => DataOriginality): Future[Ack] = {
    def update(mmap: MMap, partitionId: RingPartitionId, id: UUID, mapVal: MapVal) = {
      mmap.get(partitionId) match {
        case None              => mmap + (partitionId -> Map(id -> mapVal))
        case Some(partitionMap) => mmap + (partitionId -> (partitionMap ++ Map(id -> mapVal)))
      }
    }

    val (id, mapVal) = cmd match {
      case StoragePutData.Single(data)                => (data.id, MapVal(data, None))
      case StoragePutData.Conflict(uid, data1, data2) => (uid, MapVal(data1, Option(data2)))
    }

    resolveOriginality(id) match {
      case DataOriginality.Primary(partitionId) => primaries = update(primaries, partitionId, id, mapVal)
      case DataOriginality.Replica(partitionId) => replicas  = update(replicas, partitionId, id, mapVal)
    }

    Ack.future
  }
}
