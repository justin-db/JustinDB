package justin.db

import java.util.UUID

import justin.db.StorageNodeActorProtocol._
import justin.db.storage.PluggableStorage

import scala.concurrent.{ExecutionContext, Future}

class LocalDataReadingService(storage: PluggableStorage)(implicit ec: ExecutionContext) {
  def apply(id: UUID): Future[StorageNodeReadingResult] = {
    storage.get(id.toString).map {
      case Some(value) => StorageNodeReadingResult.Found(Data(id, value))
      case None        => StorageNodeReadingResult.NotFound
    }.recover {
      case _           => StorageNodeReadingResult.FailedRead
    }
  }
}
