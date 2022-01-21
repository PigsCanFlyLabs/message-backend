package ca.pigscanfly.service

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.{Cookie, HttpCookiePair}
import akka.pattern.ask
import akka.util.Timeout
import ca.pigscanfly.Application.executionContext
import ca.pigscanfly.actors.GetMessageActor.{GetMessage, MessageAck}
import ca.pigscanfly.actors.SendMessageActor.{GetDeviceIdFromEmailOrPhone, GetDeviceIdSuccess, PostMessageCommand}
import ca.pigscanfly.actors.{GetMessageActor, SendMessageActor}
import ca.pigscanfly.configs.Constants
import ca.pigscanfly.configs.Constants.SwarmBaseUrl
import ca.pigscanfly.dao.UserDAO
import ca.pigscanfly.models.{MessageDelivery, MessagePost, MessageRetrieval}
import ca.pigscanfly.util.Validations

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class SwarmService(twilioService: TwilioService)(actorSystem: ActorSystem,userDAO:UserDAO)
  extends SprayJsonSupport
  with Validations {

  val getMessageActor: ActorRef = actorSystem.actorOf(GetMessageActor.props)
  val sendMessageActor: ActorRef = actorSystem.actorOf(SendMessageActor.props(userDAO))

  implicit val timeout: Timeout = Timeout(3.seconds)

  def getMessages(req: HttpRequest): Future[MessageRetrieval] = {
    val cookies = extractCookies(req.cookies)
    val messagesFut = (getMessageActor ? GetMessage(s"$SwarmBaseUrl/hive/api/v1/messages", cookies.toList)).mapTo[MessageRetrieval]

    messagesFut.map { messages =>
      messages.messageResponse.foreach { message =>
        getMessageActor ! MessageAck(s"$SwarmBaseUrl/hive/api/v1/messages/rxack", message.ackPacketId, cookies.toList)
        //TODO: Check DB to see if valid
        twilioService.sendToTwilio(Constants.EmptyString, Constants.EmptyString, message.data)
      }
    }
    messagesFut
  }

  def postMessages(from: String, to: String, data: String, req: HttpRequest): Future[MessageDelivery] = {
    val cookies = extractCookies(req.cookies)
    if(validEmailPhone(from)){
      ask(sendMessageActor, GetDeviceIdFromEmailOrPhone(from)).map{
        case response:GetDeviceIdSuccess=>
          val deviceId=response.deviceId
      }
    }
    val messagePost = MessagePost(1, 1, 1, java.util.Base64.getEncoder.encodeToString(data.getBytes()))
    (sendMessageActor ? PostMessageCommand(s"$SwarmBaseUrl/hive/api/v1/messages", messagePost, cookies.toList)).mapTo[MessageDelivery]
  }

  private def extractCookies(cookies: Seq[HttpCookiePair]): Seq[Cookie] = {
    cookies.map { cookie =>
      Cookie(cookie.name, cookie.value)
    }

  }
}
