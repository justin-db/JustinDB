package justin.db.client

import java.util.UUID

import org.scalatest.{FlatSpec, Matchers}
import spray.json.JsString

class UnmarshallersTest extends FlatSpec with Matchers {

  behavior of "Unmarshaller"

  it should "encode JSON into UUID" in {
    val uuid = UUID.randomUUID()
    val jsString = JsString(uuid.toString)

    Unmarshallers.UuidFormat.read(jsString) shouldBe uuid
  }

  it should "decode UUID into JSON" in {
    val uuid = UUID.randomUUID()
    val expectedJSON = Unmarshallers.UuidFormat.write(uuid)

    expectedJSON shouldBe JsString(uuid.toString)
  }
}
