package justin.db.storage

import java.util.UUID

import justin.db.storage.PluggableStorageProtocol.{Ack, StorageGetData}

import scala.collection.mutable
import scala.concurrent.Future

/**
  * NOT THREAD-SAFE!
  */
class InMemStorage extends PluggableStorageProtocol {

  private[this] val data = mutable.Map.empty[UUID, JustinData]

  override def get(id: UUID): Future[StorageGetData] = Future.successful {
    data.get(id)
      .map(StorageGetData.Single)
      .getOrElse(StorageGetData.None)
  }

  override def put(justinData: JustinData): Future[Ack] = {
    data += justinData.id -> justinData
    Ack.future
  }
}
