package justin.db.kryo

import com.esotericsoftware.kryo.Kryo
import com.typesafe.scalalogging.StrictLogging

class SerializerInit extends StrictLogging {

  def customize(kryo: Kryo): Unit = {
    logger.info("Initialized Kryo")

    // cluster
    kryo.register(classOf[justin.db.actors.protocol.RegisterNode], RegisterNodeSerializer, 50)

    // write -- request
    kryo.register(classOf[justin.db.actors.protocol.StorageNodeWriteDataLocal], StorageNodeWriteDataLocalSerializer, 61)

    // write -- responses
    kryo.register(classOf[justin.db.actors.protocol.StorageNodeFailedWrite],     StorageNodeWriteResponseSerializer, 70)
    kryo.register(classOf[justin.db.actors.protocol.StorageNodeSuccessfulWrite], StorageNodeWriteResponseSerializer, 71)
    kryo.register(classOf[justin.db.actors.protocol.StorageNodeConflictedWrite], StorageNodeWriteResponseSerializer, 72)

    // read - request
    kryo.register(classOf[justin.db.actors.protocol.StorageNodeLocalRead], StorageNodeLocalReadSerializer, 80)

    // read - responses
    kryo.register(classOf[justin.db.actors.protocol.StorageNodeFoundRead],      StorageNodeReadResponseSerializer, 90)
    kryo.register(classOf[justin.db.actors.protocol.StorageNodeConflictedRead], StorageNodeReadResponseSerializer, 91)
    kryo.register(classOf[justin.db.actors.protocol.StorageNodeNotFoundRead],   StorageNodeReadResponseSerializer, 92)
    kryo.register(classOf[justin.db.actors.protocol.StorageNodeFailedRead],     StorageNodeReadResponseSerializer, 93)
  }
}
