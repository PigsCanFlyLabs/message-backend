package ca.pigscanfly.configs

import ca.pigscanfly.models.DBConfig
import com.typesafe.config._

object AdminConstants {

  val config: Config = ConfigFactory.load

  val adminHost = config.getString("admin.host")
  val adminPort: Int = config.getInt("admin.port")


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
