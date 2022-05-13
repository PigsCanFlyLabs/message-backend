package ca.pigscanfly.actors

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.HttpHeader
import akka.pattern.pipe
import ca.pigscanfly.Application.{executionContext, swarmMessageClient}
import ca.pigscanfly.actors.GetMessageActor.{GetEmailOrPhoneFromDeviceId, GetMessage, GetPhoneOrEmailSuccess, MessageAck, Response, SwarmLogin}
import ca.pigscanfly.dao.UserDAO
import ca.pigscanfly.httpClient.HttpClient
import ca.pigscanfly.models.LoginCredentials

import scala.concurrent.Future


object GetMessageActor {

  def props(userDAO: UserDAO): Props = Props(new GetMessageActor(userDAO))

  sealed trait Command

  sealed trait Response

  case class GetEmailOrPhoneFromDeviceId(deviceId: Long) extends Command

  case class GetMessage(url: String, headers: List[HttpHeader]) extends Command

  case class SwarmLogin(url: String, loginCredentials: LoginCredentials) extends Command

  case class MessageAck(url: String, packageId: Int, headers: List[HttpHeader]) extends Command

  case class GetPhoneOrEmailSuccess(phone: Option[String], email: Option[String]) extends Response

}

class GetMessageActor(userDAO: UserDAO) extends Actor with HttpClient with SprayJsonSupport with ActorLogging {
  override def receive: Receive = {
    case getMessageCommand: GetMessage =>
      log.info(s"SendMessageActor: Fetching messages from Swarm url: ${getMessageCommand.url}, headers: ${getMessageCommand.headers}")
      swarmMessageClient.getMessages(getMessageCommand.url, getMessageCommand.headers).pipeTo(sender())
    case messageAck: MessageAck =>
      log.info(s"SendMessageActor: Sending acknowledgement for messages to Swarm. url: ${messageAck.url}, packetId:${messageAck.packageId}, headers: ${messageAck.headers}")
      swarmMessageClient.ackMessage(messageAck.url, messageAck.packageId, messageAck.headers)
    case getCookies: SwarmLogin =>
      log.info(s"SendMessageActor: Getting logged in Swarm. url: ${getCookies.url}")
      swarmMessageClient.login(getCookies.url, getCookies.loginCredentials).pipeTo(sender())
    case GetEmailOrPhoneFromDeviceId(deviceId: Long) =>
      log.info(s"SendMessageActor: Getting email or phone for deviceId: $deviceId")
      val res: Future[Response] = getEmailOrPhoneFromDeviceId(deviceId)
      res.pipeTo(sender())
    case _ =>
      log.info("SendMessageActor: Unhandled request")
  }

  /**
   * This method retrieves emailId or phoneNumber from deviceId
   * @param deviceId
   * @return Future[Response]
   */
  def getEmailOrPhoneFromDeviceId(deviceId: Long): Future[Response] = {
    userDAO.getEmailOrPhoneFromDeviceId(deviceId).map {
      case Some((phone, email)) => GetPhoneOrEmailSuccess(phone, email)
      case None => GetPhoneOrEmailSuccess(None, None)
    }
  }
}
