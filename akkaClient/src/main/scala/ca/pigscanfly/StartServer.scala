package ca.pigscanfly

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import ca.pigscanfly.configs.Constants.{ServerHost, ServerPort}
import ca.pigscanfly.controllers.SwarmController

object StartServer extends App {

  implicit val system = ActorSystem("parent-sharing-system")

  val notificationController = new SwarmController(system)
  val routerHandler = notificationController.routes
  val bindingFuture = Http().newServerAt(ServerHost, ServerPort).bind(routerHandler)
}
