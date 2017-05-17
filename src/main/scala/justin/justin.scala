package justin

import com.typesafe.config.Config
import com.wacai.config.annotation._

@conf
trait justin extends Configurable {

  val `node-id`: Int = 0

  val ring = new {
    val `members-count`: Int = 3
    val partitions: Int = 21
  }

  val replication = new {
    val N: Int = 3
  }

  val `storage-type`: String = "justin.db.storage.InMemStorage"

  val http = new {
    val interface: String = "0.0.0.0"
    val port: Int = 9000
  }

  val `netty-tcp-hostname`: String = "localhost"
  val `netty-tcp-port`: Int        = 2551
  val `cluster-seed-nodes`         = List("akka.tcp://justin-db-cluster-system@localhost:2551", "akka.tcp://justin-db-cluster-system@localhost:2552")
}

class JustinConfig(val config: Config) extends justin

object JustinConfig {
  def apply(config: Config): JustinConfig = new JustinConfig(config)
}