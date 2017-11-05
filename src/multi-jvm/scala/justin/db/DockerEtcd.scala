package justin.db

import com.whisk.docker.impl.dockerjava.DockerKitDockerJava
import com.whisk.docker.{DockerContainer, DockerReadyChecker}

trait DockerEtcd extends DockerKitDockerJava {
  val DefaultEtcdPort = 2379

  val etcdContainer: DockerContainer = DockerContainer("quay.io/coreos/etcd:v2.3.7", name = Some("etcd"))
    .withPorts(DefaultEtcdPort -> Some(DefaultEtcdPort))
    .withEnv(s"ETCD_LISTEN_CLIENT_URLS=http://0.0.0.0:$DefaultEtcdPort", s"ETCD_ADVERTISE_CLIENT_URLS=http://0.0.0.0:$DefaultEtcdPort")
    .withReadyChecker(DockerReadyChecker.LogLineContains("set the initial cluster version"))

  abstract override def dockerContainers: List[DockerContainer] = etcdContainer :: super.dockerContainers
}
