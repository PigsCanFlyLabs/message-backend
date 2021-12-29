package ca.pigscanfly

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import ca.pigscanfly.controllers.SwarmController

object StartServer extends App { //TODO Remove App

  implicit val system = ActorSystem("parent-sharing-system")

  val notificationController = new SwarmController // TODO WILL BE HANDLED BY MACWIRE DI
  val routerHandler = notificationController.routes
  val bindingFuture = Http().newServerAt("localhost", 8080).bind(routerHandler)
}
