package ca.pigscanfly.controllers

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.headers.Cookie
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import ca.pigscanfly.CirceSupport._
import ca.pigscanfly.actors.GetMessageActor.{GetMessage, MessageAck}
import ca.pigscanfly.actors.SendMessageActor.PostMessageCommand
import ca.pigscanfly.actors.{GetMessageActor, SendMessageActor}
import ca.pigscanfly.configs.Constants.SwarmBaseUrl
import ca.pigscanfly.models.{MessageDelivery, MessagePost, MessageRetrieval}
import io.circe.syntax._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class SwarmController(actorSystem: ActorSystem) {

  val getMessageActor: ActorRef = actorSystem.actorOf(GetMessageActor.props)
  val sendMessageActor: ActorRef = actorSystem.actorOf(SendMessageActor.props)

  implicit val timeout = Timeout(3.seconds)

  def routes: Route = path("messages") {
    get {
      extractRequest { req =>
        val cookies = req.cookies.map { cookie =>
          Cookie(cookie.name, cookie.value)
        }
        val messagesFut = (getMessageActor ? GetMessage(s"$SwarmBaseUrl/hive/api/v1/messages", cookies.toList)).mapTo[MessageRetrieval]
        messagesFut.map { messages =>
          messages.messageResponse.map { message =>
            getMessageActor ! MessageAck(s"$SwarmBaseUrl/hive/api/v1/messages/rxack", message.ackPacketId, cookies.toList)
          }
        }
        complete(HttpEntity(ContentTypes.`application/json`, messagesFut.map(_.asJson).toString))
      }
    }
  } ~ path("send/messages") {
    post {
      entity(as[MessagePost]) { messagePost =>
        extractRequest { req =>
          val cookies = req.cookies.map { cookie =>
            Cookie(cookie.name, cookie.value)
          }
          val response = (sendMessageActor ? PostMessageCommand(s"$SwarmBaseUrl/hive/api/v1/messages", messagePost, cookies.toList)).mapTo[MessageDelivery].map(_.asJson)
          complete(HttpEntity(ContentTypes.`application/json`, response.toString))
        }
      }
    }
  }
}
