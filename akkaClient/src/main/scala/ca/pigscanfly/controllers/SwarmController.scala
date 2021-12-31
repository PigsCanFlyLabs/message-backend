package ca.pigscanfly.controllers

import akka.http.scaladsl.model.headers.Cookie
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ca.pigscanfly.CirceSupport._
import ca.pigscanfly.SwarmStart.newMessage
import ca.pigscanfly.configs.Constants.SwarmBaseUrl
import ca.pigscanfly.models.MessagePost
import io.circe.syntax._

import scala.concurrent.ExecutionContext.Implicits.global

class SwarmController {

  def routes: Route = path("messages") {
    get {
      extractRequest { req =>
        val cookies = req.cookies.map { cookie =>
          Cookie(cookie.name, cookie.value)
        }
        val messages = newMessage.getMessages(s"$SwarmBaseUrl/hive/api/v1/messages", cookies.toList).map(_.asJson)
        complete(HttpEntity(ContentTypes.`application/json`, messages.toString))
      }
    }
  } ~ path("send/messages") {
    post {
      entity(as[MessagePost]) { messagePost =>
        extractRequest { req =>
          val cookies = req.cookies.map { cookie =>
            Cookie(cookie.name, cookie.value)
          }
          val response = newMessage.sendMessage(s"$SwarmBaseUrl/hive/api/v1/messages", messagePost, cookies.toList).map(_.asJson)
          complete(HttpEntity(ContentTypes.`application/json`, response.toString))
        }
      }
    }
  }
}
