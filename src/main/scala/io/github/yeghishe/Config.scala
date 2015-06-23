package io.github.yeghishe

import com.typesafe.config.ConfigFactory

trait Config {
  private val config = ConfigFactory.load()
}
