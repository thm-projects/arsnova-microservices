package de.thm.arsnova.keywordservice

import de.thm.arsnova.keywordservice.Config
import org.flywaydb.core.Flyway

trait MigrationConfig extends Config {

  private val flyway = new Flyway()
  flyway.setDataSource(databaseUrl, databaseUser, databasePassword)

  def migrate(): Unit = {
    flyway.migrate()
  }

  def reloadSchema(): Unit = {
    flyway.clean()
    flyway.migrate()
  }

}
