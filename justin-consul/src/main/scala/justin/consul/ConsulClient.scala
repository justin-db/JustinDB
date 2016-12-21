package justin.consul

import java.net.URL

import akka.actor.{ActorSystem, Address}
import com.orbitz.consul.Consul
import com.orbitz.consul.model.health.ServiceHealth
import com.orbitz.consul.option.{ConsistencyMode, ImmutableQueryOptions}

import scala.collection.JavaConversions._

class ConsulClient(config: ConsulClientConfig)(implicit actorSystem: ActorSystem) {

  def getServiceAddresses: List[Address] = serviceNodes.getResponse.toList.map(serviceHealth2Address)

  private def serviceHealth2Address(sh: ServiceHealth) = Address("akka.tcp", actorSystem.name, sh.getService.getAddress, sh.getService.getPort)

  private def serviceNodes = {
    ConsulClient.buildConsul(config.host, config.port)
      .healthClient()
      .getHealthyServiceInstances(config.serviceName.name, ConsulClient.buildQueryOpts)
  }
}

object ConsulClient {

  private def buildConsul(consulHost: ConsulHost, consulPort: ConsulPort): Consul = {
    Consul.builder()
      .withUrl(new URL("http", consulHost.host, consulPort.port, ""))
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
