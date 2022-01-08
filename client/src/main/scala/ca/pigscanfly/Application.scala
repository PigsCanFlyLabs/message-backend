package ca.pigscanfly

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import ca.pigscanfly.configs.Constants.{ServerHost, ServerPort}
import ca.pigscanfly.controllers.SwarmController
import ca.pigscanfly.service.{SwarmService, TwilioService}

object Application extends App {

  implicit val system = ActorSystem("parent-sharing-system")

  val twilioService = new TwilioService()
  val swarmService = new SwarmService(twilioService)(system)
  val notificationController = new SwarmController(swarmService)
  val routerHandler = notificationController.routes
  val bindingFuture = Http().newServerAt(ServerHost, ServerPort).bind(routerHandler)
}
