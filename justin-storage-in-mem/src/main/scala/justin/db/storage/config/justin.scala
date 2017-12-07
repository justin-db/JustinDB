package justin.db.storage.config

import com.wacai.config.annotation._

// $COVERAGE-OFF$
@conf
trait justin {

  val storage = new {
    val inmemory = new {
      val name: String = "In-Mem Storage"
    }
  }
}

object StorageConfig extends justin
// $COVERAGE-ON$
