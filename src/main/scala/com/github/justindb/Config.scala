package com.github.justindb

import com.typesafe.config.ConfigFactory

trait Config {
  private val config = ConfigFactory.load()
}
