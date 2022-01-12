package ca.pigscanfly.flyway


import ca.pigscanfly.models.DBConfig
import org.flywaydb.core.Flyway

import scala.util.Try

/**
 * Database migrations
 */
class FlywayService(DBConfig: DBConfig) {

  private[this] val flyway = new Flyway()
  flyway.setDataSource(DBConfig.url, DBConfig.user, DBConfig.password)
  flyway.setBaselineOnMigrate(true)
  flyway.setSchemas(DBConfig.schema)

  def migrateDatabaseSchema(): Int = Try(flyway.migrate()).getOrElse {
    flyway.repair()
    flyway.migrate()
  }

  def dropDatabase(): Unit = flyway.clean()
}
