package ca.pigscanfly

import akka.http.scaladsl.Http
import ca.pigscanfly.SwarmStart._
import ca.pigscanfly.configs.Constants.{ServerHost, ServerPort}
import ca.pigscanfly.controllers.SwarmController
import ca.pigscanfly.service.{SwarmService, TwilioService}
import org.slf4j.{Logger, LoggerFactory}

import scala.util.{Failure, Success}

object Application extends App {

  protected val logger: Logger = LoggerFactory.getLogger(this.getClass)

  val twilioService = new TwilioService()
  val swarmService = new SwarmService(twilioService)(system)
  val notificationController = new SwarmController(swarmService)
  val routerHandler = notificationController.routes
  val bindingFuture = Http().newServerAt(ServerHost, ServerPort).bind(routerHandler)

  bindingFuture.onComplete {
    case Success(binding) ⇒
      val localAddress = binding.localAddress
      logger.info(s"Swarm Application is listening on ${localAddress.getHostName}:${localAddress.getPort}!!!!")
    case Failure(exception) => logger.info("Unable to start Exposure Data API Service due to ", exception)
  }
}
