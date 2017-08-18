package logdb

import java.io._

import logdb.serializer.LogDBSerializer

/**
  * Temporary solution. Will be replaced by full standalone implementation https://github.com/justin-db/LogDB
  */
class LogDB[K, V](dir: File)(implicit serializer: LogDBSerializer[K, V]) {

  private var memtable: Map[K, V] = Map.empty[K, V]

  // restore data during LogDB init
  {
    memtable = loadAllData()
    println("membtable: " + memtable)
  }

  def save(key: K, value: V): Unit = {
    val out = new FileOutputStream(dir, true)
    val serializedData = serializer.serialize(key, value)

    try {
      out.write(serializedData)
      out.getFD.sync()
    } finally {
      out.close()
    }

    memtable += (key -> value)
  }

  def get(key: K): Option[V] = memtable.get(key)

  private[this] def loadAllData(): Map[K, V] = {
    var updates: Map[K, V] = Map.empty[K, V]

    val raf = new RandomAccessFile(dir, "r")
    raf.seek(0)
    val fileSize: Long = raf.length()
    var offset: Long = 0

    while(offset < fileSize) {
      val (key, value) = serializer.deserialize(raf)
      offset = raf.getFilePointer
      updates += ((key, value))
    }

    updates
  }
}
