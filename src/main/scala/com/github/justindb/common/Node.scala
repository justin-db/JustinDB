package com.github.justindb.common

case class Node(id: Int, name: String, hash: Int = 0) {

  type RecordHash = Int
  type RecordId = (String, RecordHash)

  private var store: Map[RecordId, Record[_]] = Map.empty

  def addRecord(key: String, recordHash: RecordHash, v: Record[_]): Unit = {
    store = store + (((key, recordHash), v))
  }

  def getRecord(key: String, recordHash: RecordHash): Record[_] = store((key, recordHash))
}

