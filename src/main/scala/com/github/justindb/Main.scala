package com.github.justindb

import akka.actor.ActorSystem

object Main extends App {
  private implicit val system = ActorSystem()
}
