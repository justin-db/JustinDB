package com.justindb.actors

import akka.actor.{ Actor, Props }
import akka.routing.FromConfig
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem

class ConsistentHashingActor extends Actor {

  var nodes = IndexedSeq.empty[ActorRef]

  override def receive: Receive = {
    case AddNode(n) => nodes :+ n
    case _ => println("xxx")
  }
}

sealed trait ConsistentHashingMsg
case class AddNode(node: ActorRef) extends ConsistentHashingMsg
