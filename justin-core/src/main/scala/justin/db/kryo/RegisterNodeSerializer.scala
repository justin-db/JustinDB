package justin.db.kryo

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, Serializer}
import justin.consistent_hashing.NodeId
import justin.db.actors.protocol.RegisterNode

object RegisterNodeSerializer extends Serializer[RegisterNode] {
  override def write(kryo: Kryo, output: Output, registerNode: RegisterNode): Unit = {
    output.writeInt(registerNode.nodeId.id)
  }

  override def read(kryo: Kryo, input: Input, `type`: Class[RegisterNode]): RegisterNode = {
    RegisterNode(NodeId(input.readInt()))
  }
}
