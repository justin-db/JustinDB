package justin.db.storage.config

import com.wacai.config.annotation._

// $COVERAGE-OFF$
@conf
trait justin {

  val storage = new {
    val rocksdb = new {
      val name: String = "RocksDB storage"
      val `journal-path`: String = "."
    }
  }
}

object StorageConfig extends justin
// $COVERAGE-ON$
