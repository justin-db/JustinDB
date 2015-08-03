import com.justindb.actors._
import com.justindb.Key
import akka.actor.ActorSystem
import akka.actor.Actor
import akka.testkit.{TestKit, TestActorRef, TestProbe}
import org.scalatest.Matchers
import org.scalatest.WordSpecLike
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.duration._
import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.language.postfixOps
import scala.util.{Success, Failure}
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

      actorRef ! NodeRegistration(Key("1"))
      actorRef ! NodeRegistration(Key("2"))
      actorRef ! NodeRegistration(Key("3"))

      actorRef.underlyingActor.ring.size shouldBe 3
    }

    "get proper sequence of nodes key from the Ring" in {
      val tester = TestProbe()
      val actorRef = TestActorRef[ConsistentHashingActor]

      tester.send(actorRef, NodeRegistration(Key("100")))
      tester.send(actorRef, NodeRegistration(Key("1")))
      tester.send(actorRef, NodeRegistration(Key("15")))
      tester.send(actorRef, NodeRegistration(Key("20")))

      tester.send(actorRef, GetNodesKey)
      tester.expectMsg(5 seconds, NodesKeys(Iterable(Key("20"), Key("15"), Key("1"), Key("100"))))
    }

  }

}