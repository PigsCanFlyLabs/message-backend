package ca.pigscanfly.configs

import com.typesafe.config._

object Constants {

  val config: Config = ConfigFactory.load

  val SwarmBaseUrl: String = config.getString("swarm.server.base.url")
  val SendgridSecretKey: String = config.getString("sendgrid.secret.key")

}
