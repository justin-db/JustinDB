package justin.consul

import akka.actor.{ActorSystem, Address}
import com.google.common.net.HostAndPort
import com.orbitz.consul.Consul
import com.orbitz.consul.option.{ConsistencyMode, ImmutableQueryOptions}

import scala.collection.JavaConversions._

class ConsulClient(config: ConsulClientConfig) {

  def getServiceAddresses(implicit actorSystem: ActorSystem): List[Address] = {
    val serviceNodes = ConsulClient.buildConsul(config.host, config.port)
      .healthClient()
      .getHealthyServiceInstances(config.serviceName.name, ConsulClient.buildQueryOpts)

    serviceNodes.getResponse.toList.map { node =>
      Address("akka.tcp", actorSystem.name, node.getService.getAddress, node.getService.getPort)
    }
  }
}

object ConsulClient {

  private def buildConsul(consulHost: ConsulHost, consulPort: ConsulPort): Consul = {
    com.orbitz.consul.Consul.builder()
      .withHostAndPort(new HostAndPort(consulHost.host, consulPort.port))
      .build()
  }

  private def buildQueryOpts: ImmutableQueryOptions = {
    ImmutableQueryOptions
      .builder()
      .consistencyMode(ConsistencyMode.CONSISTENT)
      .build()
  }
}

case class ConsulHost(host: String) extends AnyVal
case class ConsulPort(port: Int) extends AnyVal
case class ConsulServiceName(name: String) extends AnyVal
case class ConsulClientConfig(host: ConsulHost, port: ConsulPort,serviceName: ConsulServiceName)
