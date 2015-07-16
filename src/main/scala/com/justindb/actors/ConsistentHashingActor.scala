package com.justindb.actors

import scala.concurrent.duration.DurationInt

import akka.actor.{ Actor, Props }
import akka.routing.FromConfig

class ConsistentHashingActor extends Actor {

  val router = context.actorOf(FromConfig.props(), name = "consumerRouter")
  
  override def receive: Receive = {
    case "send" => println("works")
  }
}

object ConsistentHashingActor {
  def props = Props(new ConsistentHashingActor)
}

