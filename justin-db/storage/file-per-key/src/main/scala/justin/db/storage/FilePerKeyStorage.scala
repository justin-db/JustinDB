package justin.db.storage

import java.io.{File, PrintWriter}
import FilePerKeyStorage._

class FilePerKeyStorage extends PluggableStorage {
  override def get(key: String): Option[String] = {
    val source = scala.io.Source.fromFile(fileNameWithTxtExtenstion(key))
    try {
      Option(source.getLines().mkString)
    } catch {
      case ex: Exception => None // TODO: log about failure
    } finally {
      source.close()
    }
  }

  override def put(key: String, value: String): Unit = {
    val saver = new PrintWriter(new File(fileNameWithTxtExtenstion(key)))
    try {
      saver.println(value)
    } catch {
      case ex: Exception => // TODO: log about failure
    } finally {
      saver.close()
    }
  }
}

object FilePerKeyStorage {
  def fileNameWithTxtExtenstion(fileName: String) = s"$fileName.txt"
}
