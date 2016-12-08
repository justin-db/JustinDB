package justin.db

import justin.db.StorageNodeActorProtocol._
import justin.db.storage.PluggableStorageProtocol.StorageGetData
import justin.db.storage.PluggableStorageProtocol

import scala.concurrent.{ExecutionContext, Future}

class LocalDataWriter(storage: PluggableStorageProtocol)(implicit ec: ExecutionContext) {
  def apply(data: Data): Future[StorageNodeWritingResult] = {
    storage.get(data.id).flatMap {
      case StorageGetData.None             => storage.put(data).map(_ => StorageNodeWritingResult.SuccessfulWrite)
      case StorageGetData.Conflicted(_, _) => Future.successful(StorageNodeWritingResult.FailedWrite)
      case StorageGetData.Single(existed)  =>
        // jesli juz istnieje pod takim kluczem zapisana wartosc to sprawdzamy relacje zegarow wektorowych
        // if data.VC <= saved.VC            - StorageNodeWritingResult.FailedWrite
        // if data.VC conflict with saved.VC - StorageNodeWritingResult.ConflictedWrite
        // if data.VC > saved.VC             - StorageNodeWritingResult.SuccessfulWrite

        ???
    } recover { case _ => StorageNodeWritingResult.FailedWrite }
  }
}
