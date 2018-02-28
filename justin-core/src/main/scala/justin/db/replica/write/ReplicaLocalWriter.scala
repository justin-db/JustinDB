package justin.db.replica.write

import justin.db.Data
import justin.db.actors.protocol.{StorageNodeConflictedWrite, StorageNodeFailedWrite, StorageNodeSuccessfulWrite, StorageNodeWriteResponse}
import justin.db.storage.PluggableStorageProtocol.StorageGetData
import justin.db.storage.{GetStorageProtocol, PutStorageProtocol}
import justin.db.vectorclocks.VectorClockComparator
import justin.db.vectorclocks.VectorClockComparator.VectorClockRelation

import scala.concurrent.{ExecutionContext, Future}

class ReplicaLocalWriter(storage: GetStorageProtocol with PutStorageProtocol)(implicit ec: ExecutionContext) {

  def apply(newData: Data): Future[StorageNodeWriteResponse] = {
    storage.get(newData.id).flatMap {
      case StorageGetData.None            => putSingleSuccessfulWrite(newData)
      case StorageGetData.Single(oldData) => handleExistedSingleData(oldData, newData)
    } recover { case _                    => StorageNodeFailedWrite(newData.id) }
  }

  private def handleExistedSingleData(oldData: Data, newData: Data) = {
    new VectorClockComparator().apply(oldData.vclock, newData.vclock) match {
      case VectorClockRelation.Predecessor => Future.successful(StorageNodeFailedWrite(newData.id))
      case VectorClockRelation.Conflict    => Future.successful(StorageNodeConflictedWrite(oldData, newData))
      case VectorClockRelation.Consequent  => putSingleSuccessfulWrite(newData)
    }
  }

  private def putSingleSuccessfulWrite(newData: Data) = {
    storage.put(newData).map(_ => StorageNodeSuccessfulWrite(newData.id))
  }
}
