package justin.db.client

import java.util.UUID

import justin.db.Data
import justin.db.replication.{R, W}

import scala.concurrent.Future

trait StorageNodeClient {
  def get(id: UUID, r: R): Future[GetValueResponse]
  def write(data: Data, w: W): Future[WriteValueResponse]
}

sealed trait GetValueResponse
object GetValueResponse {
  case class Found(data: Data)                    extends GetValueResponse
  case object NotFound                            extends GetValueResponse
  case class Failure(error: String)               extends GetValueResponse
  case class Conflicted(data1: Data, data2: Data) extends GetValueResponse
}

sealed trait WriteValueResponse
object WriteValueResponse {
  case object Success               extends WriteValueResponse
  case object Conflict              extends WriteValueResponse
  case class Failure(error: String) extends WriteValueResponse
}
