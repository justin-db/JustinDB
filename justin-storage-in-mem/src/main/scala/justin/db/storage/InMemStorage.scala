package justin.db.storage

import java.util.UUID

import justin.db.Data
import justin.db.storage.PluggableStorageProtocol.{Ack, DataOriginality, StorageGetData, StoragePutData}

import scala.concurrent.{ExecutionContext, Future}

/**
  * NOT THREAD-SAFE!
  */
class InMemStorage(implicit ec: ExecutionContext) extends PluggableStorageProtocol {
  import scala.collection.mutable

  private case class MapVal(data1: Data, data2: Option[Data])

  private var values = mutable.Map.empty[UUID, MapVal]

  // TODO: handle resolving of originality of Data
  override def get(id: UUID)(resolveOriginality: (UUID) => DataOriginality): Future[StorageGetData] = Future.successful  {
    values.get(id) match {
      case None                             => StorageGetData.None
      case Some(MapVal(data1, Some(data2))) => StorageGetData.Conflicted(data1, data2)
      case Some(MapVal(data1, None))        => StorageGetData.Single(data1)
    }
  }

  override def put(cmd: StoragePutData): Future[Ack] = {
    values = cmd match {
      case StoragePutData.Single(data)               => values + (data.id -> MapVal(data, None))
      case StoragePutData.Conflict(id, data1, data2) => values + (id -> MapVal(data1, Option(data2)))
    }
    Ack.future
  }
}
