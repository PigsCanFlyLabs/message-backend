package ca.pigscanfly.service

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpHeader, HttpRequest}
import akka.http.scaladsl.model.headers.{Cookie, HttpCookiePair}
import akka.pattern.ask
import akka.util.Timeout
import ca.pigscanfly.Application.executionContext
import ca.pigscanfly.actors.GetMessageActor.{GetMessage, SwarmLogin, MessageAck}
import ca.pigscanfly.actors.SendMessageActor.{GetDeviceIdFromEmailOrPhone, GetDeviceIdSuccess, PostMessageCommand}
import ca.pigscanfly.actors.{GetMessageActor, SendMessageActor}
import ca.pigscanfly.configs.Constants
import ca.pigscanfly.configs.Constants.SwarmBaseUrl
import ca.pigscanfly.dao.UserDAO
import ca.pigscanfly.models.{LoginCredentials,MessageDelivery, MessagePost, MessageRetrieval}
import ca.pigscanfly.util.{ProtoUtils, Validations}
import ca.pigscanfly.proto.MessageDataPB.{Message, MessageDataPB, Protocol}

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class SwarmService(twilioService: TwilioService)(actorSystem: ActorSystem,userDAO:UserDAO)
  extends SprayJsonSupport
  with Validations
  with ProtoUtils {

  val getMessageActor: ActorRef = actorSystem.actorOf(GetMessageActor.props)
  val sendMessageActor: ActorRef = actorSystem.actorOf(SendMessageActor.props(userDAO))

  implicit val timeout: Timeout = Timeout(3.seconds)

  def swarmLogin(loginCredentials:LoginCredentials): Future[Seq[HttpHeader]] = {
    (getMessageActor ? SwarmLogin(s"$SwarmBaseUrl/login",loginCredentials)).mapTo[Seq[HttpHeader]]
  }

  def getMessages(cookies: Seq[HttpHeader]): Future[MessageRetrieval] = {
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
    if (validEmailPhone(from)) {
      ask(sendMessageActor, GetDeviceIdFromEmailOrPhone(from)).flatMap {
        case response: GetDeviceIdSuccess =>
          response.deviceId.fold(throw new Exception(s"Device is not present for phone number $from")) { deviceId =>
            val msg = Message(data, to, Protocol.values(2))
            //TODO Gather msg in time window
            val messageDataPB: MessageDataPB = MessageDataPB(1, Seq(msg), false)
            val messagePost = MessagePost(1, deviceId, 1, encodePostMessage(messageDataPB))
            (sendMessageActor ? PostMessageCommand(s"$SwarmBaseUrl/hive/api/v1/messages", messagePost, cookies.toList)).mapTo[MessageDelivery]
          }
      }
    } else {
      throw new Exception(s"Message received from $from i.e. is not a phone number")
    }
  }

  def extractCookies(cookies: Seq[HttpCookiePair]): Seq[Cookie] = {
    cookies.map { cookie =>
      Cookie(cookie.name, cookie.value)
    }

  }
}
