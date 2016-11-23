package justin.db.client

import java.util.UUID

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import justin.db.{Data, StorageNodeActorRef}
import justin.db.client.StorageNodeRouter.PutValue
import justin.db.client.StorageNodeRouter._
import justin.db.replication.{R, W}
import org.scalatest.{FlatSpec, Matchers}
import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import scala.concurrent.Future

class StorageNodeRouterTest extends FlatSpec with Matchers with ScalatestRouteTest {

  behavior of "Storage Node Router"

  /**
    * GET part
    */
  it should "get OK result for specific id and r" in {
    val value  = "value"
    val id     = UUID.randomUUID().toString
    val r      = 1
    val router = new StorageNodeRouter(getFound(value))

    Get(s"/get?id=$id&r=$r") ~> Route.seal(router.routes) ~> check {
      status                       shouldBe StatusCodes.OK
      responseAs[String].parseJson shouldBe JsObject("value" -> JsString(value))
    }
  }

  it should "get NotFound result for specific id and r" in {
    val value  = "value"
    val id     = UUID.randomUUID().toString
    val r      = 1
    val router = new StorageNodeRouter(notFound(value))

    Get(s"/get?id=$id&r=$r") ~> Route.seal(router.routes) ~> check {
      status                       shouldBe StatusCodes.NotFound
      responseAs[String].parseJson shouldBe JsObject("value" -> JsString(s"Not found value with id $id"))
    }
  }

  it should "get BadRequest result for specific id and r" in {
    val value  = "value"
    val id     = UUID.randomUUID().toString
    val r      = 1
    val error  = "Bad Request"
    val router = new StorageNodeRouter(badRequest(error))

    Get(s"/get?id=$id&r=$r") ~> Route.seal(router.routes) ~> check {
      status                       shouldBe StatusCodes.BadRequest
      responseAs[String].parseJson shouldBe JsObject("value" -> JsString(error))
    }
  }

  private def getFound(value: String) = new HttpStorageNodeClient(StorageNodeActorRef(null)) {
    override def get(id: UUID, r: R): Future[GetValueResponse] = Future.successful(GetValueResponse.Found(value))
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
  it should "get NoContent result for successfully written data" in {
    val putValue = PutValue(id = UUID.randomUUID(), value = "value", w = 3)
    val router   = new StorageNodeRouter(successfulWrite(putValue))

    Post("/put", putValue) ~> Route.seal(router.routes) ~> check {
      status shouldBe StatusCodes.NoContent
    }
  }

  private def successfulWrite(putValue: PutValue) = new HttpStorageNodeClient(StorageNodeActorRef(null)) {
    override def write(w: W, data: Data): Future[WriteValueResponse] = Future.successful(WriteValueResponse.Success)
  }
}
