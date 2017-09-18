package de.thm.arsnova.roomservice

import com.typesafe.config.ConfigFactory

trait Config {
  private val config = ConfigFactory.load()
}
