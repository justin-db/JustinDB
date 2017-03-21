package justin.http_api

import com.typesafe.config.Config

trait ServiceConfig {
  protected def config: Config

  lazy val `nodes-id`: Int          = config.getInt("node.id")
  lazy val `ring-cluster-size`: Int = config.getInt("ring.cluster-size")
  lazy val `ring-partitions`: Int   = config.getInt("ring.partitions")
  lazy val `replication-n`: Int     = config.getInt("justin-db.replication.N")

  lazy val `http-interface`: String = config.getString("http.interface")
  lazy val `http-port`: Int         = config.getInt("http.port")
}
