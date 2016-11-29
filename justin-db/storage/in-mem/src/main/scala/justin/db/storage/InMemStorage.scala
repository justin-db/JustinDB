package justin.db.storage

import java.util.UUID

import justin.db.Data

import scala.concurrent.{ExecutionContext, Future}

/**
  * NOT THREAD-SAFE!
  */
class InMemStorage(implicit ec: ExecutionContext) extends PluggableStorage {
  import scala.collection.mutable

  private var values = mutable.Map.empty[UUID, Data]

  override def get(id: UUID): Future[Option[Data]] = Future.successful(values.get(id))

  override def put(data: Data): Future[Unit] = { values = values + (data.id -> data); Future.successful(()) }
}
