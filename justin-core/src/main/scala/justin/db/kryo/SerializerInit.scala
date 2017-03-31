package justin.db.kryo

import com.esotericsoftware.kryo.Kryo
import com.typesafe.scalalogging.StrictLogging

class SerializerInit extends StrictLogging {

  def customize(kryo: Kryo): Unit = {
    logger.info("Initialized Kryo")

    // cluster
    kryo.register(classOf[justin.db.actors.protocol.RegisterNode], RegisterNodeSerializer, 50)

    // write -- input
    kryo.register(classOf[justin.db.actors.protocol.StorageNodeWriteDataLocal], StorageNodeWriteDataLocalSerializer, 51)
    // write -- output
    kryo.register(classOf[justin.db.actors.protocol.StorageNodeFailedWrite], StorageNodeFailedWriteSerializer, 52)
    kryo.register(classOf[justin.db.actors.protocol.StorageNodeSuccessfulWrite], StorageNodeSuccessfulWriteSerializer, 60)
  }
}
