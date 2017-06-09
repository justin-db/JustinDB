package justin.db.actors.protocol

case class MultiDataCenterContacts(contacts: List[String])
case class DataCenterReplica(writeDataReq: StorageNodeWriteRequest)
