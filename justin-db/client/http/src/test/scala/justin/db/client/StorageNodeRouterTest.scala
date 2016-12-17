package justin.db.client

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import justin.db.client.HttpRouter.{PutValue, _}
import justin.db.replication.{R, W}
import justin.db.{Data, StorageNodeActorRef}
import org.scalatest.{FlatSpec, Matchers}
import spray.json.{JsString, _}

import scala.concurrent.Future

class StorageNodeRouterTest extends FlatSpec with Matchers with ScalatestRouteTest {

  behavior of "Http Router"

  /**
    * GET part
    */
  it should "get \"OK\" http code for successful read result" in {
    val value  = "value"
    val id     = UUID.randomUUID()
    val r      = 1
    val router = new HttpRouter(getFound(Data(id, value)))

    Get(s"/get?id=${id.toString}&r=$r") ~> Route.seal(router.routes) ~> check {
      status                       shouldBe StatusCodes.OK
      responseAs[String].parseJson shouldBe JsObject("value" -> JsString(value))
    }
  }

  it should "get \"MultipleChoices\" http code for read conflicted data" in {
    val r      = 1
    val id     = UUID.randomUUID()
    val data1  = Data(id, "value-1")
    val data2  = Data(id, "value-2")
    val router = new HttpRouter(getConflicted(data1, data2))

    Get(s"/get?id=${id.toString}&r=$r") ~> Route.seal(router.routes) ~> check {
      status                       shouldBe StatusCodes.MultipleChoices
      responseAs[String].parseJson shouldBe JsObject("value" -> JsString("Multiple Choices"))
    }
  }

  it should "get \"NotFound\" http code for missed searched data" in {
    val value  = "value"
    val id     = UUID.randomUUID().toString
    val r      = 1
    val router = new HttpRouter(notFound(value))

    Get(s"/get?id=$id&r=$r") ~> Route.seal(router.routes) ~> check {
      status                       shouldBe StatusCodes.NotFound
      responseAs[String].parseJson shouldBe JsObject("value" -> JsString(s"Not found value with id $id"))
    }
  }

  it should "get \"BadRequest\" http code for unsuccessful read result" in {
    val value  = "value"
    val id     = UUID.randomUUID().toString
    val r      = 1
    val error  = "Bad Request"
    val router = new HttpRouter(badRequest(error))

    Get(s"/get?id=$id&r=$r") ~> Route.seal(router.routes) ~> check {
      status                       shouldBe StatusCodes.BadRequest
      responseAs[String].parseJson shouldBe JsObject("value" -> JsString(error))
    }
  }

  private def getFound(data: Data) = new HttpStorageNodeClient(StorageNodeActorRef(null)) {
    override def get(id: UUID, r: R): Future[GetValueResponse] = Future.successful(GetValueResponse.Found(data))
  }

  private def getConflicted(data1: Data, data2: Data) = new HttpStorageNodeClient(StorageNodeActorRef(null)) {
    override def get(id: UUID, r: R): Future[GetValueResponse] = Future.successful(GetValueResponse.Conflicted(data1, data2))
  }

  private def notFound(value: String) = new HttpStorageNodeClient(StorageNodeActorRef(null)) {
    override def get(id: UUID, r: R): Future[GetValueResponse] = Future.successful(GetValueResponse.NotFound)
  }

  private def badRequest(error: String) = new HttpStorageNodeClient(StorageNodeActorRef(null)) {
    override def get(id: UUID, r: R): Future[GetValueResponse] = Future.successful(GetValueResponse.Failure(error))
  }

  /**
    * PUT part
    */
  it should "get \"NoContent\" http code for successful write result" in {
    val putValue = PutValue(id = UUID.randomUUID(), value = "value", w = 3)
    val router   = new HttpRouter(successfulWrite(putValue))

    Post("/put", putValue) ~> Route.seal(router.routes) ~> check {
      status shouldBe StatusCodes.NoContent
    }
  }

  it should "get \"BadRequest\" http code for unsuccessful write result" in {
    val putValue = PutValue(id = UUID.randomUUID(), value = "value", w = 3)
    val error    = "unsuccessfully written data"
    val router   = new HttpRouter(unsuccessfulWrite(error))

    Post("/put", putValue) ~> Route.seal(router.routes) ~> check {
      status                       shouldBe StatusCodes.BadRequest
      responseAs[String].parseJson shouldBe JsObject("value" -> JsString(error))
    }
  }

  it should "get \"MultipleChoices\" http code for conflicted write result" in {
    val putValue = PutValue(id = UUID.randomUUID(), value = "value", w = 3)
    val router   = new HttpRouter(conclictedWrite)

    Post("/put", putValue) ~> Route.seal(router.routes) ~> check {
      status                       shouldBe StatusCodes.MultipleChoices
      responseAs[String].parseJson shouldBe JsObject("value" -> JsString("Multiple Choices"))
    }
  }

  private def successfulWrite(putValue: PutValue) = new HttpStorageNodeClient(StorageNodeActorRef(null)) {
    override def write(data: Data, w: W): Future[WriteValueResponse] = Future.successful(WriteValueResponse.Success)
  }

  private def unsuccessfulWrite(error: String) = new HttpStorageNodeClient(StorageNodeActorRef(null)) {
    override def write(data: Data, w: W): Future[WriteValueResponse] = Future.successful(WriteValueResponse.Failure(error))
  }

  private def conclictedWrite = new HttpStorageNodeClient(StorageNodeActorRef(null)) {
    override def write(data: Data, w: W): Future[WriteValueResponse] = Future.successful(WriteValueResponse.Conflict)
  }
}
