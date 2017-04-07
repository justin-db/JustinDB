package justin.db.kryo

import com.esotericsoftware.kryo.Kryo
import org.scalatest.{FlatSpec, Matchers}

class SerializerInitTest extends FlatSpec with Matchers {

  behavior of "SerializerInit"

  it should "init Kryo serializer" in {
    val kryo = new Kryo()
    val serializerInit = new SerializerInit()
    serializerInit.customize(kryo)

    // cluster
    val classId_50 = 50
    kryo.getRegistration(classId_50).getId          shouldBe 50
    kryo.getRegistration(classId_50).getSerializer  shouldBe RegisterNodeSerializer
    kryo.getRegistration(classId_50).getType        shouldBe classOf[justin.db.actors.protocol.RegisterNode]


    // write -- request
    val classId_60 = 60
    kryo.getRegistration(classId_60).getId          shouldBe 60
    kryo.getRegistration(classId_60).getSerializer  shouldBe StorageNodeWriteDataLocalSerializer
    kryo.getRegistration(classId_60).getType        shouldBe classOf[justin.db.actors.protocol.StorageNodeWriteDataLocal]

    // write -- responses
    val classId_70 = 70
    kryo.getRegistration(classId_70).getId          shouldBe 70
    kryo.getRegistration(classId_70).getSerializer  shouldBe StorageNodeWriteResponseSerializer
    kryo.getRegistration(classId_70).getType        shouldBe classOf[justin.db.actors.protocol.StorageNodeFailedWrite]

    val classId_71 = 71
    kryo.getRegistration(classId_71).getId          shouldBe 71
    kryo.getRegistration(classId_71).getSerializer  shouldBe StorageNodeWriteResponseSerializer
    kryo.getRegistration(classId_71).getType        shouldBe classOf[justin.db.actors.protocol.StorageNodeSuccessfulWrite]

    val classId_72 = 72
    kryo.getRegistration(classId_72).getId          shouldBe 72
    kryo.getRegistration(classId_72).getSerializer  shouldBe StorageNodeWriteResponseSerializer
    kryo.getRegistration(classId_72).getType        shouldBe classOf[justin.db.actors.protocol.StorageNodeConflictedWrite]

    // read - request
    val classId_80 = 80
    kryo.getRegistration(classId_80).getId          shouldBe 80
    kryo.getRegistration(classId_80).getSerializer  shouldBe StorageNodeLocalReadSerializer
    kryo.getRegistration(classId_80).getType        shouldBe classOf[justin.db.actors.protocol.StorageNodeLocalRead]

    // read - responses
    val classId_90 = 90
    kryo.getRegistration(classId_90).getId          shouldBe 90
    kryo.getRegistration(classId_90).getSerializer  shouldBe StorageNodeReadResponseSerializer
    kryo.getRegistration(classId_90).getType        shouldBe classOf[justin.db.actors.protocol.StorageNodeFoundRead]

    val classId_91 = 91
    kryo.getRegistration(classId_91).getId          shouldBe 91
    kryo.getRegistration(classId_91).getSerializer  shouldBe StorageNodeReadResponseSerializer
    kryo.getRegistration(classId_91).getType        shouldBe classOf[justin.db.actors.protocol.StorageNodeConflictedRead]

    val classId_92 = 92
    kryo.getRegistration(classId_92).getId          shouldBe 92
    kryo.getRegistration(classId_92).getSerializer  shouldBe StorageNodeReadResponseSerializer
    kryo.getRegistration(classId_92).getType        shouldBe classOf[justin.db.actors.protocol.StorageNodeNotFoundRead]

    val classId_93 = 93
    kryo.getRegistration(classId_93).getId          shouldBe 93
    kryo.getRegistration(classId_93).getSerializer  shouldBe StorageNodeReadResponseSerializer
    kryo.getRegistration(classId_93).getType        shouldBe classOf[justin.db.actors.protocol.StorageNodeFailedRead]
  }
}
