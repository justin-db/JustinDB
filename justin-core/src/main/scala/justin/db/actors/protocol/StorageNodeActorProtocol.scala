package justin.db.actors.protocol

import java.util.UUID

import justin.db.Data
import justin.db.replica.{R, W}

sealed trait StorageNodeReadRequest
case class StorageNodeLocalRead(id: UUID) extends StorageNodeReadRequest
object StorageNodeReadRequest {
  case class Replicated(r: R, id: UUID) extends StorageNodeReadRequest // Is this ever wired between remotes Actors?
}
sealed trait StorageNodeReadResponse
case class StorageNodeFoundRead(data: Data)                 extends StorageNodeReadResponse
case class StorageNodeConflictedRead(conflicts: List[Data]) extends StorageNodeReadResponse
case class StorageNodeNotFoundRead(id: UUID)                extends StorageNodeReadResponse
object StorageNodeReadResponse {
  case object FailedRead                 extends StorageNodeReadResponse
}

sealed trait StorageNodeWriteData
case class StorageNodeWriteDataLocal(data: Data) extends StorageNodeWriteData
object StorageNodeWriteData {
  case class Replicate(w: W, data: Data) extends StorageNodeWriteData // Is this ever wired between remotes Actors?
}

sealed trait StorageNodeWriteResponse
case class StorageNodeSuccessfulWrite(id: UUID)                     extends StorageNodeWriteResponse
case class StorageNodeFailedWrite(id: UUID)                         extends StorageNodeWriteResponse
case class StorageNodeConflictedWrite(oldData: Data, newData: Data) extends StorageNodeWriteResponse
