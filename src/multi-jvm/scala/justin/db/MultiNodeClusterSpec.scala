package justin.db

import java.util.concurrent.ConcurrentHashMap

import akka.actor.Address
import akka.cluster.Cluster
import akka.remote.testconductor.RoleName
import akka.remote.testkit.MultiNodeSpec
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.Suite

import scala.concurrent.duration.{FiniteDuration, _}

object MultiNodeClusterSpec {

  val commonBaseConfig: Config = ConfigFactory.parseString(
    s"""
       |akka.loglevel = INFO
       |akka.log-config-on-start = false
       |akka.log-dead-letters = off
       |akka.log-dead-letters-during-shutdown = off
       |akka.remote.log-remote-lifecycle-events = off
    """.stripMargin
  )
}

trait MultiNodeClusterSpec extends Suite with STMultiNodeSpec { self: MultiNodeSpec ⇒

  private val cachedAddresses = new ConcurrentHashMap[RoleName, Address]

  def initialParticipants: Int = roles.size

  /**
    * Get the cluster node to use.
    */
  def cluster: Cluster = Cluster(system)

  /**
    * Lookup the Address for the role.
    *
    * Implicit conversion from RoleName to Address.
    *
    * It is cached, which has the implication that stopping
    * and then restarting a role (jvm) with another address is not
    * supported.
    */
  implicit def address(role: RoleName): Address = {
    cachedAddresses.get(role) match {
      case null ⇒
        val address = node(role).address
        cachedAddresses.put(role, address)
        address
      case address ⇒ address
    }
  }

  /**
    * Wait until the expected number of members has status Up has been reached.
    * Also asserts that nodes in the 'canNotBePartOfMemberRing' are *not* part of the cluster ring.
    */
  def awaitMembersUp(numberOfMembers: Int, canNotBePartOfMemberRing: Set[Address] = Set(), timeout: FiniteDuration = 10.seconds): Unit = {
    awaitAssert(
      a = {
        cluster.state.members.size shouldBe numberOfMembers

        println("cluster state: " + cluster.state)

        cluster.state.members.map(_.address) intersect canNotBePartOfMemberRing shouldBe empty
      },
      max = timeout,
      interval = 2.seconds
    )
    ()
  }
}
