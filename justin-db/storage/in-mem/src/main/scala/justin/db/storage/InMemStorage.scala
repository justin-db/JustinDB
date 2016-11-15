package justin.db.storage

import scala.concurrent.{ExecutionContext, Future}

class InMemStorage(implicit ec: ExecutionContext) extends PluggableStorage {
  import scala.collection.mutable

  private var values = mutable.Map.empty[String, String]

  override def get(key: String): Future[Option[String]] = Future.successful(values.get(key))
  override def put(key: String, value: String): Future[Unit] = { values = values + ((key,value)); Future.successful(()) }
}
