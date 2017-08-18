package logdb.serializer

import java.io.{ByteArrayOutputStream, DataOutputStream, RandomAccessFile}

trait LogDBSerializer[K, V] {
  protected def writeKey(key: K): Array[Byte]
  protected def readKey(serialized: Array[Byte]): K

  protected def writeValue(value: V): Array[Byte]
  protected def readValue(serialized: Array[Byte]): V

  final def serialize(key: K, value: V): Array[Byte] = {
    def serializeLength(key: Int) = {
      val out = new ByteArrayOutputStream()
      val out2 = new DataOutputStream(out)
      out2.writeInt(key)
      out.toByteArray
    }

    val serializedKey: Array[Byte] = writeKey(key)
    val serializedValue: Array[Byte] = writeValue(value)

    val serializedSizeKey: Array[Byte]   = serializeLength(serializedKey.length)
    val serializedSizeValue: Array[Byte] = serializeLength(serializedValue.length)

    serializedSizeKey ++ serializedSizeValue ++ serializedKey ++ serializedValue
  }

  final def deserialize(raf: RandomAccessFile): (K, V) = {
    val sizeKey  = raf.readInt()
    val sizeValue = raf.readInt()

    val bufferKey = new Array[Byte](sizeKey)
    val bufferValue = new Array[Byte](sizeValue)

    raf.read(bufferKey, 0, sizeKey)
    raf.read(bufferValue, 0, sizeValue)

    (readKey(bufferKey), readValue(bufferValue))
  }
}
