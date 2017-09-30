package justin.db.storage

import java.io.File

object JustinDriver {

  def load(clazz: String)(eventualJournalFile: File): PluggableStorageProtocol = {
    val loadedClass = Class.forName(clazz)

    loadedClass.getSimpleName match {
      case "PersistentStorage" | "RocksDBStorage" => instantiate(loadedClass)(eventualJournalFile)
      case _                                      => loadedClass.newInstance().asInstanceOf[PluggableStorageProtocol]
    }
  }

  private[this] def instantiate[T](clazz: java.lang.Class[_])(args: AnyRef*): T = {
    val constructor = clazz.getConstructors()(0)
    constructor.newInstance(args:_*).asInstanceOf[T]
  }
}
