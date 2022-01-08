package ca.pigscanfly.service

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.Cookie
import akka.pattern.ask
import akka.util.Timeout
import ca.pigscanfly.actors.GetMessageActor.{GetMessage, MessageAck}
import ca.pigscanfly.actors.{GetMessageActor, SendMessageActor}
import ca.pigscanfly.configs.Constants
import ca.pigscanfly.configs.Constants.SwarmBaseUrl
import ca.pigscanfly.models.MessageRetrieval

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class SwarmService(twilioService: TwilioService)(actorSystem: ActorSystem) {

  val getMessageActor: ActorRef = actorSystem.actorOf(GetMessageActor.props)
  val sendMessageActor: ActorRef = actorSystem.actorOf(SendMessageActor.props)

  implicit val timeout: Timeout = Timeout(3.seconds)

  def getMessages(req: HttpRequest): Future[MessageRetrieval] = {
    val cookies = req.cookies.map { cookie =>
      Cookie(cookie.name, cookie.value)
    }
    val messagesFut = (getMessageActor ? GetMessage(s"$SwarmBaseUrl/hive/api/v1/messages", cookies.toList)).mapTo[MessageRetrieval]

    messagesFut.map { messages =>
      messages.messageResponse.foreach { message =>
        getMessageActor ! MessageAck(s"$SwarmBaseUrl/hive/api/v1/messages/rxack", message.ackPacketId, cookies.toList)
        twilioService.sendToTwilio(Constants.EmptyString, Constants.EmptyString, message.data)
      }
    }
    messagesFut
  }

}
