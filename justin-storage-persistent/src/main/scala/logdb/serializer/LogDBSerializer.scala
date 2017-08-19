package logdb.serializer

import java.io.{ByteArrayOutputStream, DataOutputStream, RandomAccessFile}
import java.nio.ByteBuffer
import java.util.UUID

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

  protected def uuid2bytes(uuid: UUID): Array[Byte] = {
    val bb = ByteBuffer.wrap(new Array[Byte](16))
    bb.putLong(uuid.getMostSignificantBits)
    bb.putLong(uuid.getLeastSignificantBits)
    bb.array()
  }
  protected def bytes2uuid(bytes: Array[Byte]): UUID = {
    val bb = ByteBuffer.wrap(bytes)
    val firstLong = bb.getLong
    val secondLong = bb.getLong
    new UUID(firstLong, secondLong)
  }

  protected def int2bytes(int: Int): Array[Byte] = {
    ByteBuffer.allocate(4).putInt(int).array()
  }
  protected def bytes2int(bytes: Array[Byte]): Int = {
    ByteBuffer.wrap(bytes).getInt()
  }

  protected def long2bytes(x: Long): Array[Byte] = {
    val buffer = ByteBuffer.allocate(8)
    buffer.putLong(x)
    buffer.array
  }
  protected def bytes2long(bytes: Array[Byte]): Long = {
    val buffer = ByteBuffer.allocate(8)
    buffer.put(bytes)
    buffer.flip //need flip
    buffer.getLong
  }
}
