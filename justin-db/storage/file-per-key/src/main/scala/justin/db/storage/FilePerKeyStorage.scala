package justin.db.storage

import java.io.{File, PrintWriter}

import FilePerKeyStorage._

import scala.concurrent.{ExecutionContext, Future}

class FilePerKeyStorage(implicit ec: ExecutionContext) extends PluggableStorage {
  override def get(key: String): Future[Option[String]] = {
    val source = scala.io.Source.fromFile(fileNameWithTxtExtenstion(key))
    try {
      Future.successful(Option(source.getLines().mkString))
    } catch {
      case ex: Exception => Future.successful(None) // TODO: log about failure
    } finally {
      source.close()
    }
  }

  override def put(key: String, value: String): Future[Unit] = {
    val saver = new PrintWriter(new File(fileNameWithTxtExtenstion(key)))
    try {
      saver.println(value); Future.successful(())
    } catch {
      case ex: Exception => Future.successful(()) // TODO: log about failure
    } finally {
      saver.close()
    }
  }
}

object FilePerKeyStorage {
  def fileNameWithTxtExtenstion(fileName: String) = s"$fileName.txt"
}
