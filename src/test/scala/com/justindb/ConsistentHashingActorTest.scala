import com.justindb.actors._
import com.justindb._
import akka.actor.ActorSystem
import akka.actor.Actor
import akka.testkit.{ TestKit, TestActorRef, TestProbe }
import org.scalatest.Matchers
import org.scalatest.WordSpecLike
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.duration._
import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.language.postfixOps
import scala.util.{ Success, Failure }
import scala.concurrent.ExecutionContext.Implicits.global

class ConsistentHashingActorTest extends TestKit(ActorSystem("testSystem"))
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A Consistent Hashing Actor" must {

    "add node to the Ring while its registration" in {
      val actorRef = TestActorRef[ConsistentHashingActor]

      actorRef ! ConsistentHashingActor.NodeRegistration(Key("1"))
      actorRef ! ConsistentHashingActor.NodeRegistration(Key("2"))
      actorRef ! ConsistentHashingActor.NodeRegistration(Key("3"))

      actorRef.underlyingActor.ring.size shouldBe 3
    }

    "get proper sequence of nodes key from the Ring" in {
      val tester = TestProbe()
      val actorRef = TestActorRef[ConsistentHashingActor]

      tester.send(actorRef, ConsistentHashingActor.NodeRegistration(Key("100")))
      tester.send(actorRef, ConsistentHashingActor.NodeRegistration(Key("1")))
      tester.send(actorRef, ConsistentHashingActor.NodeRegistration(Key("15")))
      tester.send(actorRef, ConsistentHashingActor.NodeRegistration(Key("20")))

      tester.send(actorRef, ConsistentHashingActor.GetNodesKey)
      tester.expectMsg(ConsistentHashingActor.NodesKeys(Iterable(Key("20"), Key("15"), Key("1"), Key("100"))))
    }

    "get node failure while trying to get record when there is no Node in the system" in {
      val tester = TestProbe()
      val actorRef = TestActorRef[ConsistentHashingActor]

      tester.send(actorRef, ConsistentHashingActor.GetRecord(Key("naive-key")))
      tester.expectMsg(ConsistentHashingActor.NodeFailure("Ring is empty, try again later."))
    }

    "get node failure while trying to save a new record when there is no Node in the system" in {
      val tester = TestProbe()
      val actorRef = TestActorRef[ConsistentHashingActor]

      tester.send(actorRef, ConsistentHashingActor.AddRecord(Key("naive-key"), Record("value")))
      tester.expectMsg(ConsistentHashingActor.NodeFailure("Ring is empty, try again later."))
    }

  }

}