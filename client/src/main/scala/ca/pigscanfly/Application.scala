package ca.pigscanfly

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import ca.pigscanfly.configs.ClientConstants.{dbConfig, serverHost, serverPort}
import ca.pigscanfly.configs.Constants.{AccountSID, AuthToken}
import ca.pigscanfly.controllers.SwarmController
import ca.pigscanfly.dao.SwarmDAO
import ca.pigscanfly.httpClient.HttpClient
import ca.pigscanfly.service.{SwarmService, TwilioService}
import com.twilio.Twilio
import org.slf4j.{Logger, LoggerFactory}
import ca.pigscanfly.db.DatabaseApi.api._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Application extends App {

  implicit val system: ActorSystem = ActorSystem("Swarm-Start")
  implicit val executionContext: ExecutionContext = system.dispatcher

  protected val logger: Logger = LoggerFactory.getLogger(this.getClass)

  val swarmMessageClient = new SwarmMessageClient() with HttpClient {
    override implicit def actorSystem: ActorSystem = system
    override implicit def executionContext: ExecutionContext = actorSystem.dispatcher
  }

  implicit val db: Database = Database.forURL(
    url = dbConfig.url,
    user = dbConfig.user,
    password = dbConfig.password,
    driver = dbConfig.driver,
    executor = AsyncExecutor("postgres",
      numThreads = dbConfig.threadsPoolCount,
      queueSize = dbConfig.queueSize)
  )

  implicit val schema: String = dbConfig.schema

  val swarmDAO = new SwarmDAO()

  val twilio: Unit = Twilio.init(AccountSID, AuthToken)
  val twilioService = new TwilioService()
  val swarmService = new SwarmService(twilioService)(system)
  val notificationController = new SwarmController(swarmService)
  val routerHandler = notificationController.routes
  val bindingFuture = Http().newServerAt(serverHost, serverPort).bind(routerHandler)

  bindingFuture.onComplete {
    case Success(binding) ⇒
      val localAddress = binding.localAddress
      logger.info(s"Application is listening on ${localAddress.getHostName}:${localAddress.getPort}!!!!")
    case Failure(exception) => logger.info("Unable to start Application Service due to ", exception)
  }
}
