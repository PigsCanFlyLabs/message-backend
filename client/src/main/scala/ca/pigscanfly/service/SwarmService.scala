package ca.pigscanfly.service

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers.{Cookie, HttpCookiePair}
import akka.pattern.ask
import akka.util.Timeout
import ca.pigscanfly.Application.executionContext
import ca.pigscanfly.actors.GetMessageActor._
import ca.pigscanfly.actors.SendMessageActor._
import ca.pigscanfly.actors.{GetMessageActor, SendMessageActor}
import ca.pigscanfly.configs.ClientConstants.{swarmPassword, swarmUserName}
import ca.pigscanfly.configs.Constants.SwarmBaseUrl
import ca.pigscanfly.dao.UserDAO
import ca.pigscanfly.models
import ca.pigscanfly.models.{LoginCredentials, MessageDelivery, MessagePost, MessageRetrieval}
import ca.pigscanfly.proto.MessageDataPB.{Message, MessageDataPB, Protocol}
import ca.pigscanfly.util.Constants._
import ca.pigscanfly.util.{ProtoUtils, Validations}

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class SwarmService(twilioService: TwilioService)(actorSystem: ActorSystem, userDAO: UserDAO)
  extends SprayJsonSupport
    with Validations
    with ProtoUtils {

  val getMessageActor: ActorRef = actorSystem.actorOf(GetMessageActor.props(userDAO))
  val sendMessageActor: ActorRef = actorSystem.actorOf(SendMessageActor.props(userDAO))

  implicit val timeout: Timeout = Timeout(3.seconds)

  def getMessages(): Future[MessageRetrieval] = {
    swarmLogin(LoginCredentials(swarmUserName, swarmPassword)).flatMap { cookies: Seq[HttpHeader] =>
      val messagesFut = (getMessageActor ? GetMessage(s"$SwarmBaseUrl/hive/api/v1/messages", cookies.toList)).mapTo[MessageRetrieval]

      messagesFut.map { messages =>
        messages.messageResponse.foreach { message: models.GetMessage =>
          getMessageActor ! MessageAck(s"$SwarmBaseUrl/hive/api/v1/messages/rxack", message.ackPacketId, cookies.toList)
        }
      }
      messagesFut
    }
  }

  def getPhoneOrEmailFromDeviceId(deviceId: Long): Future[GetPhoneOrEmailSuccess] = {
    (getMessageActor ? GetEmailOrPhoneFromDeviceId(deviceId)).mapTo[GetPhoneOrEmailSuccess]
  }

  def postMessages(from: String, to: String, data: String): Future[MessageDelivery] = {
    swarmLogin(LoginCredentials(swarmUserName, swarmPassword)).flatMap { cookies: Seq[HttpHeader] =>
      if (validateEmailPhone(from)) {
        ask(sendMessageActor, GetDeviceIdFromEmailOrPhone(from)).flatMap {
          case response: GetDeviceId =>
            response.deviceId.fold(throw new Exception(s"Device is not present for phone number $from")) { deviceId =>
              ask(sendMessageActor, CheckSubscription(deviceId)).flatMap {
                case response: CheckDeviceSubscription =>
                  response.isDisabled.fold(throw new Exception(s"Couldn't found subscription details of device ID $deviceId")) { isDeviceDisabled =>
                    if (!isDeviceDisabled) {
                      val msg = detectSource(to) match {
                        case EMAIl =>
                          Message(data, to, Protocol.values(1))
                        case SMS =>
                          Message(data, to, Protocol.values(2))
                        case UNKOWN =>
                          throw new Exception("Invalid Source")
                      }
                      //TODO Gather msg in time window
                      val messageDataPB: MessageDataPB = MessageDataPB(1, Seq(msg), false)
                      val messagePost = MessagePost(1, deviceId, 1, encodePostMessage(messageDataPB))
                      (sendMessageActor ? PostMessageCommand(s"$SwarmBaseUrl/hive/api/v1/messages", messagePost, cookies.toList)).mapTo[MessageDelivery]
                    } else {
                      throw new Exception(s"[Device ID Disabled] Can't send message from device Id $deviceId")
                    }
                  }
                case ex => throw new Exception(s"Got exception while checking Subscription for device ID $deviceId ex:: $ex")
              }
            }
          case ex => throw new Exception(s"Got exception while getting Device ID for phone number $from ex:: $ex")
        }
      } else {
        throw new Exception(s"Message received from $from i.e. is not a phone number")
      }
    }
  }

  def swarmLogin(loginCredentials: LoginCredentials): Future[Seq[HttpHeader]] = {
    (getMessageActor ? SwarmLogin(s"$SwarmBaseUrl/login", loginCredentials)).mapTo[Seq[HttpHeader]]
  }

  def extractCookies(cookies: Seq[HttpCookiePair]): Seq[Cookie] = {
    cookies.map { cookie =>
      Cookie(cookie.name, cookie.value)
    }

  }
}
