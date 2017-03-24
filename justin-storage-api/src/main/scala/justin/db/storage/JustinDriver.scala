package justin.db.storage

object JustinDriver {

  def load(clazz: String): PluggableStorageProtocol = Class.forName(clazz)
    .newInstance()
    .asInstanceOf[PluggableStorageProtocol]
}
