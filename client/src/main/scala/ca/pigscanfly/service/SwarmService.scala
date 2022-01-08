package ca.pigscanfly.service

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.{Cookie, HttpCookiePair}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.pattern.ask
import akka.util.Timeout
import ca.pigscanfly.SwarmStart.{executionContext, system}
import ca.pigscanfly.actors.GetMessageActor.{GetMessage, MessageAck}
import ca.pigscanfly.actors.SendMessageActor.PostMessageCommand
import ca.pigscanfly.actors.{GetMessageActor, SendMessageActor}
import ca.pigscanfly.configs.Constants
import ca.pigscanfly.configs.Constants.SwarmBaseUrl
import ca.pigscanfly.models.{MessageDelivery, MessagePost, MessageRetrieval}

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class SwarmService(twilioService: TwilioService)(actorSystem: ActorSystem) extends SprayJsonSupport {

  val getMessageActor: ActorRef = actorSystem.actorOf(GetMessageActor.props)
  val sendMessageActor: ActorRef = actorSystem.actorOf(SendMessageActor.props)

  implicit val timeout: Timeout = Timeout(3.seconds)

  def getMessages(req: HttpRequest): Future[MessageRetrieval] = {
    val cookies = extractCookies(req.cookies)
    val messagesFut = (getMessageActor ? GetMessage(s"$SwarmBaseUrl/hive/api/v1/messages", cookies.toList)).mapTo[MessageRetrieval]

    messagesFut.map { messages =>
      messages.messageResponse.foreach { message =>
        getMessageActor ! MessageAck(s"$SwarmBaseUrl/hive/api/v1/messages/rxack", message.ackPacketId, cookies.toList)
        twilioService.sendToTwilio(Constants.EmptyString, Constants.EmptyString, message.data)
      }
    }
    messagesFut
  }

  def postMessages(req: HttpRequest): Future[MessageDelivery] = {
    val cookies = extractCookies(req.cookies)

    val entity: Future[MessagePost] = Unmarshal(req.entity).to[MessagePost]
    entity.flatMap { messagePost =>
      val updatedMessage = messagePost.copy(data = java.util.Base64.getEncoder.encodeToString(messagePost.data.getBytes()))
      (sendMessageActor ? PostMessageCommand(s"$SwarmBaseUrl/hive/api/v1/messages", updatedMessage, cookies.toList)).mapTo[MessageDelivery]
    }
  }

  private def extractCookies(cookies: Seq[HttpCookiePair]): Seq[Cookie] = {
    cookies.map { cookie =>
      Cookie(cookie.name, cookie.value)
    }

  }
}
