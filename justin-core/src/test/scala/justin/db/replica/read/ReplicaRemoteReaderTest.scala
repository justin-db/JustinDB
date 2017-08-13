package justin.db.replica.read

import java.util.UUID

import akka.actor.{Actor, ActorSystem}
import akka.testkit.{TestActorRef, TestKit}
import justin.db.Data
import justin.db.actors.StorageNodeActorRef
import justin.db.actors.protocol._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpecLike, Matchers}

import scala.concurrent.duration._

class ReplicaRemoteReaderTest extends TestKit(ActorSystem("test-system"))
  with FlatSpecLike
  with Matchers
  with ScalaFutures {

  behavior of "Replica Remote Reader"

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(10.seconds, 50.millis)

  it should "get info back that one of the value could be found and second one is obsolete" in {
    // given
    val service = new ReplicaRemoteReader()(system.dispatcher)
    val id = UUID.randomUUID()
    val foundData = Data(id, "value")
    val notFoundId = UUID.randomUUID()
    val storageNotFoundActorRef = testActorRef(msgBack = StorageNodeNotFoundRead(notFoundId))
    val storageFoundActorRef    = testActorRef(msgBack = StorageNodeFoundRead(foundData))
    val storageNodeRefs         = List(storageNotFoundActorRef, storageFoundActorRef).map(StorageNodeActorRef)

    // when
    val readingResult = service.apply(storageNodeRefs, id)

    // then
    whenReady(readingResult) { _ shouldBe List(StorageNodeNotFoundRead(notFoundId), StorageNodeFoundRead(foundData)) }
  }

  it should "recover failed behavior of actor" in {
    // given
    val service = new ReplicaRemoteReader()(system.dispatcher)
    val id = UUID.randomUUID()
    val storageActorRef = testActorRef(new Exception)
    val storageNodeRefs = List(StorageNodeActorRef(storageActorRef))

    // when
    val readingResult = service.apply(storageNodeRefs, id)

    // then
    whenReady(readingResult) { _ shouldBe List(StorageNodeFailedRead(id)) }
  }

  private def testActorRef(msgBack: => Any) = {
    TestActorRef(new Actor {
      override def receive: Receive = {
        case StorageNodeLocalRead(id) => sender() ! msgBack
      }
    })
  }
}
