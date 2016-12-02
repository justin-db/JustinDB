package justin.db

import justin.db.StorageNodeActorProtocol._
import justin.db.storage.PluggableStorage

import scala.concurrent.{ExecutionContext, Future}

class LocalDataWriter(storage: PluggableStorage)(implicit ec: ExecutionContext) {
  def apply(data: Data): Future[StorageNodeWritingResult] = {
    storage.put(data)
      .map(_ => StorageNodeWritingResult.SuccessfulWrite)
      .recover { case _ => StorageNodeWritingResult.FailedWrite }
  }
}
