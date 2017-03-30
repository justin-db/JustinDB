package justin.db.kryo

import com.esotericsoftware.kryo.Kryo
import com.typesafe.scalalogging.StrictLogging

class SerializerInit extends StrictLogging {

  def customize(kryo: Kryo): Unit = {
    logger.info("Initialized Kryo")

    kryo.register(classOf[justin.db.actors.protocol.RegisterNode], RegisterNodeSerializer, 50)
    kryo.register(classOf[justin.db.actors.protocol.StorageNodeWriteDataLocal], StorageNodeWriteDataLocalSerializer, 51)
//    kryo.register(classOf[justin.db.actors.protocol.StorageNodeReadData.Replicated], StorageNodeWriteDataReplicateSerializer, 52)
  }
}
