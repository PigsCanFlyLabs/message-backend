package ca.pigscanfly.controllers

import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ca.pigscanfly.SwarmStart.executionContext
import ca.pigscanfly.service.SwarmService
import io.circe.syntax._

import scala.util.{Failure, Success}

class SwarmController(swarmService: SwarmService) {

  def routes: Route = path("messages") {
    get {
      extractRequest { request =>
        val result = swarmService.getMessages(request)
        onComplete(result) {
          case Success(messages) => complete(HttpEntity(ContentTypes.`application/json`, messages.asJson.toString))
          case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
        }
      }
    }
  } ~ path("send/messages") {
    post {
      extractRequest { request =>
        val result = swarmService.postMessages(request).map(_.asJson)
        onComplete(result) {
          case Success(response) => complete(HttpEntity(ContentTypes.`application/json`, response.toString))
          case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
        }
      }
    }
  }
}
