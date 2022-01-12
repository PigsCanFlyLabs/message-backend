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


  val adminHost = config.getString("admin.host")
  val adminPort: Int = config.getInt("admin.port")

  val akkaAwaitDuration: Int = config.getInt("akka.future.await.duration.mins")
  val akkaWorkerCount: Int = config.getInt("akka.akka.workers.count")

  val dbProfile = config.getString("db.config.profile")
  val dbDriver = config.getString("db.config.driver")
  val dbUrl = config.getString("db.config.url")
  val dbUser = config.getString("db.config.user")
  val dbPassword = config.getString("db.config.password")
  val dbSchema = config.getString("db.config.schema")
  val dbThreadsPoolCount = config.getInt("db.config.threads.pool.count")
  val dbQueueSize = config.getInt("db.config.queue.size")
  val dbSearchLimit = config.getInt("db.config.search.limit")

  val jwtKey = config.getString("jwt.scala.circe.key")
  val jwtExpiryDuration: Int = config.getInt("jwt.scala.circe.expire-duration-sec")

}
