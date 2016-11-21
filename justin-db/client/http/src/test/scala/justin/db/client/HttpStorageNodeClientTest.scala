package justin.db.client

import java.util.UUID

import akka.actor.{Actor, ActorSystem}
import akka.testkit.{TestActorRef, TestKit}
import justin.db.StorageNodeActorProtocol.{StorageNodeReadData, StorageNodeReadingResult}
import justin.db.replication.R
import justin.db.{Data, StorageNodeActorRef}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

class HttpStorageNodeClientTest extends TestKit(ActorSystem("test-system"))
  with FlatSpecLike
  with Matchers
  with ScalaFutures
  with BeforeAndAfterAll {

  behavior of "HTTP Storage Node Client"

  it should "handle actor's Found message for asked data" in {
    // given
    val id       = UUID.randomUUID()
    val actorRef = readTestActorRef(msgBack = StorageNodeReadingResult.Found(Data(id, "value")))
    val client   = new HttpStorageNodeClient(StorageNodeActorRef(actorRef))(system.dispatcher)

    // when
    val result = client.get(id, R(1))

    // then
    whenReady(result) { _ shouldBe GetValueResponse.Found("value") }
  }

  it should "handle actor's NotFound message for asked data" in {
    // given
    val id       = UUID.randomUUID()
    val actorRef = readTestActorRef(msgBack = StorageNodeReadingResult.NotFound)
    val client   = new HttpStorageNodeClient(StorageNodeActorRef(actorRef))(system.dispatcher)

    // when
    val result = client.get(id, R(1))

    // then
    whenReady(result) { _ shouldBe GetValueResponse.NotFound }
  }

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  private def readTestActorRef(msgBack: => StorageNodeReadingResult) = {
    TestActorRef(new Actor {
      override def receive: Receive = {
        case StorageNodeReadData.Replicated(r, id) => sender() ! msgBack
      }
    })
  }

}
