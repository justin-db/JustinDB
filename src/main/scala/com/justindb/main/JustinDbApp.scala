package com.justindb.main

import akka.actor.ActorSystem
import com.justindb.actors.ConsistentHashingActor
import com.typesafe.config.ConfigFactory

object JustinDbApp extends App {

  ConsistentHashingApp.main(Seq("2551").toArray)
  NodeApp.main(Seq("2552").toArray)
  NodeApp.main(Array.empty)
  NodeApp.main(Array.empty)

}
