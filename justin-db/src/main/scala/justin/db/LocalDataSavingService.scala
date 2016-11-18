package justin.db

import justin.db.StorageNodeActorProtocol._
import justin.db.storage.PluggableStorage

import scala.concurrent.{ExecutionContext, Future}

class LocalDataSavingService(storage: PluggableStorage)(implicit ec: ExecutionContext) {
  def apply(data: Data): Future[StorageNodeWritingResult] = {
    storage.put(data.id.toString, data.value)
      .map(_ => StorageNodeWritingResult.SuccessfulWrite)
      .recover { case _ => StorageNodeWritingResult.FailedWrite }
  }
}
