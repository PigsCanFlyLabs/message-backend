package ca.pigscanfly.configs

import ca.pigscanfly.models.DBConfig
import com.typesafe.config._

object ClientConstants {

  val config: Config = ConfigFactory.load

  val schedulerInitialDelay = config.getInt("scheduler.initial.delay")
  val schedulerInterval = config.getInt("scheduler.interval")
  val messagePollingDelay = config.getInt("scheduler.polling.delay")

  val serverHost: String = config.getString("server.host")
  val serverPort: Int = config.getInt("server.port")

  val swarmUserName = config.getString("swarm.username")
  val swarmPassword = config.getString("swarm.password")

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

  val akkaAwaitDuration: Int = config.getInt("akka.future.await.duration.mins")
  val akkaWorkerCount: Int = config.getInt("akka.akka.workers.count")

  val dbConfig = DBConfig(profile = dbProfile,
    driver = dbDriver,
    url = dbUrl,
    user = dbUser,
    password = dbPassword,
    schema = dbSchema,
    threadsPoolCount = dbThreadsPoolCount,
    queueSize = dbQueueSize,
    searchLimit = dbSearchLimit)

}
