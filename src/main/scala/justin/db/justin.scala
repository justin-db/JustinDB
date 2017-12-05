package justin.db

import com.typesafe.config.{Config, ConfigFactory}
import com.wacai.config.annotation._
import justin.db.actors.StorageNodeActor

// $COVERAGE-OFF$
@conf
trait justin extends Configurable {

  val system: String = "justin"

  val `node-id`: Int = 0

  val ring = new {
    val `members-count`: Int = 1
    val partitions: Int = 21
  }

  val replication = new {
    val N: Int = 1
  }

  val storage = new {
    val provider = "justin.db.storage.provider.InMemStorageProvider"
  }

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

class JustinDBConfig(val config: Config) extends justin

object JustinDBConfig {

  def init: JustinDBConfig = new JustinDBConfig(ConfigFactory
    .parseString(s"akka.cluster.roles = [${StorageNodeActor.role}]")
    .withFallback(ConfigFactory.load()))

}
// $COVERAGE-ON$
