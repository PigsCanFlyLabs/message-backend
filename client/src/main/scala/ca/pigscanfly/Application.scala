package ca.pigscanfly

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import ca.pigscanfly.actors.{GetMessageActor, SendMessageActor}
import ca.pigscanfly.configs.ClientConstants._
import ca.pigscanfly.configs.Constants.{AccountSID, AuthToken}
import ca.pigscanfly.controllers.SwarmController
import ca.pigscanfly.dao.UserDAO
import ca.pigscanfly.httpClient.HttpClient
import ca.pigscanfly.schedular.GetMessagesScheduler
import ca.pigscanfly.service.{SwarmService, TwilioService}
import com.twilio.Twilio
import org.slf4j.{Logger, LoggerFactory}
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Application extends App {

  implicit val system: ActorSystem = ActorSystem("Swarm-Start")
  implicit val executionContext: ExecutionContext = system.dispatcher
  val swarmMessageClient = new SwarmMessageClient() with HttpClient {
    override implicit def actorSystem: ActorSystem = system

    override implicit def executionContext: ExecutionContext = actorSystem.dispatcher
  }
  val userDAO = new UserDAO()

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

  val getMessageActor: ActorRef = system.actorOf(GetMessageActor.props(userDAO, swarmMessageClient))
  val sendMessageActor: ActorRef = system.actorOf(SendMessageActor.props(userDAO, swarmMessageClient))

  implicit val searchLimit: Int = dbConfig.searchLimit
  val twilio: Unit = Twilio.init(AccountSID, AuthToken)
  val twilioService = new TwilioService()
  val swarmService = new SwarmService(twilioService)(userDAO, swarmMessageClient)
  val swarmController = new SwarmController(swarmService, sendMessageActor, getMessageActor)
  val routerHandler = swarmController.routes
  val bindingFuture = Http().newServerAt(serverHost, serverPort).bind(routerHandler)
  protected val logger: Logger = LoggerFactory.getLogger(this.getClass)
  system.actorOf(GetMessagesScheduler.props(swarmService, twilioService, getMessageActor))

  bindingFuture.onComplete {
    case Success(binding) ⇒
      val localAddress = binding.localAddress
      logger.info(s"Application is listening on ${localAddress.getHostName}:${localAddress.getPort}!!!!")
    case Failure(exception) => logger.info("Unable to start Application Service due to ", exception)
  }
}
