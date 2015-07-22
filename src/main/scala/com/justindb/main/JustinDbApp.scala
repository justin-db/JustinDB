package com.justindb

import akka.actor.ActorSystem
import com.justindb.actors.ConsistentHashingActor
import com.typesafe.config.ConfigFactory

object JustingDbApp extends App {

  ConsistentHashingApp.main(Seq("2551").toArray)
  NodeApp.main(Seq("2552").toArray)
  NodeApp.main(Array.empty)
  NodeApp.main(Array.empty)

}
