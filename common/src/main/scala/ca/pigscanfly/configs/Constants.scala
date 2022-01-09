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

  val dbProfile = config.getString("dbconfig.profile")
  val dbDriver = config.getString("dbconfig.driver")
  val dbUrl = config.getString("dbconfig.url")
  val dbUser = config.getString("dbconfig.user")
  val dbPassword = config.getString("dbconfig.password")
  val dbSchema = config.getString("dbconfig.schema")
  val dbThreadsPoolCount = config.getInt("dbconfig.threads.pool.count")
  val dbQueueSize = config.getInt("dbconfig.queue.size")
  val dbSearchLimit = config.getInt("dbconfig.search.limit")
}
