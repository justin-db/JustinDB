package justin.http_api

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import justin.db.client.{ActorRefStorageNodeClient, GetValueResponse, WriteValueResponse}
import justin.db.replica.W
import justin.db.Data
import justin.db.actors.StorageNodeActorRef
import justin.db.replica.{R, W}
import justin.http_api.HttpRouter.PutValue
import org.scalatest.{FlatSpec, Matchers}
import spray.json.{JsString, _}

import scala.concurrent.Future

class HttpRouterTest extends FlatSpec with Matchers with ScalatestRouteTest {

  behavior of "Http Router"

  /**
    * GET part
    */
  it should "get \"OK\" http code for successful read result" in {
    val id     = UUID.randomUUID()
    val data   = Data(id, "value")
    val r      = 1
    val router = new HttpRouter(getFound(data))

    Get(s"/get?id=${id.toString}&r=$r") ~> Route.seal(router.routes) ~> check {
      status                       shouldBe StatusCodes.OK
      responseAs[String].parseJson shouldBe JsObject("value" -> JsString(data.value))
      header[VectorClockHeader]    shouldBe Some(VectorClockHeader(data.vclock))
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
      header[VectorClockHeader]    shouldBe None
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
      header[VectorClockHeader]    shouldBe None
    }
  }

  it should "get \"InternalServerError\" http code for unsuccessful read result" in {
    val value  = "value"
    val id     = UUID.randomUUID().toString
    val r      = 1
    val error  = "Internal Server Error"
    val router = new HttpRouter(internalServerError(error))

    Get(s"/get?id=$id&r=$r") ~> Route.seal(router.routes) ~> check {
      status                       shouldBe StatusCodes.InternalServerError
      responseAs[String].parseJson shouldBe JsObject("value" -> JsString(error))
      header[VectorClockHeader]    shouldBe None
    }
  }

  private def getFound(data: Data) = new ActorRefStorageNodeClient(StorageNodeActorRef(null)) {
    override def get(id: UUID, r: R): Future[GetValueResponse] = Future.successful(GetValueResponse.Found(data))
  }

  private def notFound(value: String) = new ActorRefStorageNodeClient(StorageNodeActorRef(null)) {
    override def get(id: UUID, r: R): Future[GetValueResponse] = Future.successful(GetValueResponse.NotFound)
  }

  private def badRequest(error: String) = new ActorRefStorageNodeClient(StorageNodeActorRef(null)) {
    override def get(id: UUID, r: R): Future[GetValueResponse] = Future.successful(GetValueResponse.Failure(error))
  }

  private def internalServerError(error: String) = new ActorRefStorageNodeClient(StorageNodeActorRef(null)) {
    override def get(id: UUID, r: R): Future[GetValueResponse] = Future.failed(new Exception(error))
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

  private def successfulWrite(putValue: PutValue) = new ActorRefStorageNodeClient(StorageNodeActorRef(null)) {
    override def write(data: Data, w: W): Future[WriteValueResponse] = Future.successful(WriteValueResponse.Success)
  }

  private def unsuccessfulWrite(error: String) = new ActorRefStorageNodeClient(StorageNodeActorRef(null)) {
    override def write(data: Data, w: W): Future[WriteValueResponse] = Future.successful(WriteValueResponse.Failure(error))
  }

  private def conclictedWrite = new ActorRefStorageNodeClient(StorageNodeActorRef(null)) {
    override def write(data: Data, w: W): Future[WriteValueResponse] = Future.successful(WriteValueResponse.Conflict)
  }
}
