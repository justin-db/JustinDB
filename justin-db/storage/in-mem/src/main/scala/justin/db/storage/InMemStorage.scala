package justin.db.storage

import java.util.UUID

import justin.db.Data

import scala.concurrent.{ExecutionContext, Future}

/**
  * NOT THREAD-SAFE!
  */
class InMemStorage(implicit ec: ExecutionContext) extends PluggableStorageProtocol {
  import scala.collection.mutable

  private case class MapVal(data1: Data, data2: Option[Data])

  private var values = mutable.Map.empty[UUID, MapVal]

  override def get(id: UUID): Future[StorageGetData] = Future.successful {
    values.get(id) match {
      case None                             => StorageGetData.None
      case Some(MapVal(data1, Some(data2))) => StorageGetData.Conflicted(data1, data2)
      case Some(MapVal(data1, None))        => StorageGetData.Single(data1)
    }
  }

  override def put(data: Data): Future[Unit] = Future.successful {
    ???
  }
}
