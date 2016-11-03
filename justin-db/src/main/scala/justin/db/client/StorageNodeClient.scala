package justin.db.client

import java.util.UUID

import scala.concurrent.Future

trait StorageNodeClient {
  def get(id: UUID, r: ReadFactor): Future[GetValueResponse]
  def write(value: String, w: WriteFactor): Future[GetValueResponse]
}

sealed trait GetValueResponse
object GetValueResponse {
  case class Found(value: String) extends GetValueResponse
  case object NotFound extends GetValueResponse
  case class Failure(error: String) extends GetValueResponse
}

sealed trait WriteValueResponse
object WriteValueResponse {
  case object Success extends WriteValueResponse
  case class Failure(error: String) extends WriteValueResponse
}

case class ReadFactor(r: Int) extends AnyVal
case class WriteFactor(w: Int) extends AnyVal
