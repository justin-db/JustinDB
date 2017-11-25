package justin

import com.typesafe.config.Config
import com.wacai.config.annotation._

// $COVERAGE-OFF$
@conf
trait justin extends Configurable {

  val system: String = "justin"

  val `node-id`: Int = 0

  val ring = new {
    val `members-count`: Int = 3
    val partitions: Int = 21
  }

  val replication = new {
    val N: Int = 3
  }

  val `storage-type`: String = "justin.db.storage.InMemStorage"
  val `storage-journal-path`: String = "/var/justindb/journal"

  val http = new {
    val interface: String = "0.0.0.0"
    val port: Int = 9000
  }

  val `netty-tcp-hostname`: String = "localhost"
  val `netty-tcp-port`: Int        = 2551

  val `netty-tcp-bindhostname`: String = "localhost"
  val `netty-tcp-bindport`: Int        = 2551

  val dc = new {
    val `cross-data-center-connections`: Int = 1
    val `self-data-center` = "dc1"
  }
}

class JustinConfig(val config: Config) extends justin

object JustinConfig {
  def apply(config: Config): JustinConfig = new JustinConfig(config)
}
// $COVERAGE-ON$
