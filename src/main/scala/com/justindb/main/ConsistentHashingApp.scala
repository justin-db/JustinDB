package com.justindb.main

import akka.actor.ActorSystem
import com.justindb.actors.ConsistentHashingActor
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import com.justindb.{Key, Record}
import scala.concurrent.duration._
import scala.util.Random
import com.justindb.actors.AddRecord

object ConsistentHashingApp extends App {

  val port = if (args.isEmpty) "0" else args(0)
  val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
    withFallback(ConfigFactory.parseString(s"akka.cluster.roles = [ringrole]")).
    withFallback(ConfigFactory.load())

  val system = ActorSystem("ClusterSystem", config)
  val consistentHashingActor = system.actorOf(Props[ConsistentHashingActor], name = "ringrole")

  import system.dispatcher
  system.scheduler.schedule(2.seconds, 2.seconds) {

    implicit val timeout = Timeout(5 seconds)

    val record = Record[String](Key(Random.nextString(10)), "content-random-nth-special")

    consistentHashingActor ? AddRecord(record) onSuccess {
      case result => println(result)
    }
  }
}
