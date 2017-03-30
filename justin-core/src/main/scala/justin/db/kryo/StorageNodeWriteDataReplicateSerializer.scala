package justin.db.kryo

import com.esotericsoftware.kryo.{Kryo, Serializer}
import com.esotericsoftware.kryo.io.{Input, Output}
import justin.db.actors.protocol.StorageNodeWriteData

object StorageNodeWriteDataReplicateSerializer extends Serializer[StorageNodeWriteData.Replicate] {
  override def write(kryo: Kryo, output: Output, replicate: StorageNodeWriteData.Replicate): Unit = ???

  override def read(kryo: Kryo, input: Input, `type`: Class[StorageNodeWriteData.Replicate]): StorageNodeWriteData.Replicate = ???
}
