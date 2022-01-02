package ca.pigscanfly

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import ca.pigscanfly.controllers.SwarmController

object StartServer extends App {

  implicit val system = ActorSystem("parent-sharing-system")

  val notificationController = new SwarmController(system)
  val routerHandler = notificationController.routes
  val bindingFuture = Http().newServerAt("localhost", 8080).bind(routerHandler)
}
