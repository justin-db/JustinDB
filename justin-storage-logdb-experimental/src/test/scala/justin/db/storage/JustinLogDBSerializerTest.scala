package justin.db.storage

import java.io.{File, FileOutputStream, RandomAccessFile}
import java.util.UUID

import org.scalatest.{FlatSpec, Matchers}

import scala.util.Random

class JustinLogDBSerializerTest extends FlatSpec with Matchers {

  behavior of "LogDB serializer adapter for JustinDB"

  it should "serialize and deserialize single payload" in {
    val justinData = JustinData(id = UUID.randomUUID(), value = "this is exemplary value",  vclock = "very-long-vclock", timestamp = 11234L)
    val ser = new JustinLogDBSerializer()
    val journalFile: File = File.createTempFile("journal", ".tmp")

    val bytes = ser.serialize(key = justinData.id, value = justinData)
    saveToJournalFile(bytes, journalFile)
    val (key, value) = ser.deserialize(new RandomAccessFile(journalFile, "r"))

    key   shouldBe justinData.id
    value shouldBe justinData
  }

  it should "serialize and deserialize multiple payloads" in {
    val justinData = List(
      JustinData(id = UUID.randomUUID(), value = "this is exemplary value 1",  vclock = "very-long-vclock1", timestamp = new Random().nextLong()),
      JustinData(id = UUID.randomUUID(), value = "this is exemplary value 2",  vclock = "very-long-vclock2", timestamp = new Random().nextLong()),
      JustinData(id = UUID.randomUUID(), value = "this is exemplary value 3",  vclock = "very-long-vclock3", timestamp = new Random().nextLong())
    )
    val ser = new JustinLogDBSerializer()
    val journalFile: File = File.createTempFile("journal", ".tmp")

    val bytes = justinData.map(p => ser.serialize(key = p.id, value = p))
    bytes.foreach(b => saveToJournalFile(b, journalFile))
    val raf = new RandomAccessFile(journalFile, "r")
    val (key1, value1) = ser.deserialize(raf)
    val (key2, value2) = ser.deserialize(raf)
    val (key3, value3) = ser.deserialize(raf)

    key1   shouldBe justinData(0).id
    value1 shouldBe justinData(0)
    key2   shouldBe justinData(1).id
    value2 shouldBe justinData(1)
    key3   shouldBe justinData(2).id
    value3 shouldBe justinData(2)
  }

  private def saveToJournalFile(data: Array[Byte], journalFile: File) = {
    val out = new FileOutputStream(journalFile, true)
    try {
      out.write(data)
      out.getFD.sync()
    } finally {
      out.close()
    }
  }
}
