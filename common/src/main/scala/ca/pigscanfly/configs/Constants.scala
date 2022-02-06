package ca.pigscanfly.configs

import com.typesafe.config._

object Constants {

  val config: Config = ConfigFactory.load

  val SwarmBaseUrl: String = config.getString("swarm.server.base.url")
  val SendgridSecretKey: String = config.getString("sendgrid.secret.key")
  val EmptyString: String = ""
  val AccountSID: String = config.getString("twilio.account_sid")
  val AuthToken: String = config.getString("twilio.auth_token")
  
  val jwtKey = config.getString("jwt.scala.circe.key")
  val jwtExpiryDuration: Int = config.getInt("jwt.scala.circe.expire-duration-sec")

  val SmtpAddress = config.getString("smtp-config.server")
  val SmtpPort = config.getInt("smtp-config.port")
  val SmtpFromEmail = config.getString("smtp-config.fromEmail")
  val SmtpEmailUsername = config.getString("smtp-config.username")
  val SmtpEmailPassword = config.getString("smtp-config.password")

}
