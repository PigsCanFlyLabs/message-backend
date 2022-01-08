package ca.pigscanfly.configs

import com.typesafe.config._

object Constants {

  val config: Config = ConfigFactory.load

  val ServerHost: String = config.getString("server.host")
  val ServerPort: Int = config.getInt("server.port")

  val SwarmBaseUrl: String = config.getString("swarm.server.base.url")
  val SendgridSecretKey: String = config.getString("sendgrid.secret.key")
  val EmptyString: String = ""
  val AccountSID: String = config.getString("twilio.account_sid")
  val AuthToken: String = config.getString("twilio.auth_token")

}
