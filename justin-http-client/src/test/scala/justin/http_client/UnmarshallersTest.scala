package justin.http_client

import java.util.UUID

import org.scalatest.{FlatSpec, Matchers}
import spray.json.{DeserializationException, JsNumber, JsString}

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

  it should "handle not expected format of JSON" in {
    val jsNumber = JsNumber(1)

    intercept[DeserializationException] {
      Unmarshallers.UuidFormat.read(jsNumber)
    }
  }

  it should "handle wrong format of UUID" in {
    val fakeUUID = "1-2-3-4"
    val jsString = JsString(fakeUUID)

    intercept[DeserializationException] {
      Unmarshallers.UuidFormat.read(jsString)
    }
  }
}
