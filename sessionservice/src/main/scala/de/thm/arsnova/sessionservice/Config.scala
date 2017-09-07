package de.thm.arsnova.sessionservice

import com.typesafe.config.ConfigFactory

trait Config {
  private val config = ConfigFactory.load()
}
