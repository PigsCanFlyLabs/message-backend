package ca.pigscanfly.controllers

import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ca.pigscanfly.Application.executionContext
import ca.pigscanfly.service.SwarmService
import io.circe.syntax._
import org.slf4j.{Logger, LoggerFactory}

import scala.util.{Failure, Success}

class SwarmController(swarmService: SwarmService) {
  protected val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def routes: Route = path("messages") {
    get {
      val result = swarmService.getMessages()
      onComplete(result) {
        case Success(messages) => complete(HttpEntity(ContentTypes.`application/json`, messages.asJson.toString))
        case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
      }
    }
  } ~ path("messages") {
    post {
      formFields("From", "To", "Body") { (from, to, body) =>
        val result = swarmService.postMessages(from, to, body).map(_.asJson)
        onComplete(result) {
          case Success(response) => complete(HttpEntity(ContentTypes.`application/json`, response.toString))
          case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
        }
      }
    }
  }
}


