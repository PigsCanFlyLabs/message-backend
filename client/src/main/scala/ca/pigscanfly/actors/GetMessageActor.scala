package ca.pigscanfly.actors

import akka.actor.{Actor, Props}
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

  def props(userDAO:UserDAO): Props = Props(new GetMessageActor(userDAO))

  sealed trait Command
  sealed trait Response

  case class GetEmailOrPhoneFromDeviceId(deviceId:Long) extends Command
  case class GetMessage(url: String, headers: List[HttpHeader]) extends Command
  case class SwarmLogin(url: String, loginCredentials: LoginCredentials) extends Command
  case class MessageAck(url: String, packageId: Int, headers: List[HttpHeader]) extends Command

  case class GetPhoneOrEmailSuccess(phone:Option[String], email:Option[String]) extends Response

}

class GetMessageActor(userDAO:UserDAO) extends Actor with HttpClient with SprayJsonSupport {
  override def receive: Receive = {
    case getMessageCommand: GetMessage =>
      //            Future(MessageRetrieval(List(Message(1,1,1,"sdfasf",1,1,1,"sfdasdfa",1,1,"strkngr")))).pipeTo(sender())
      swarmMessageClient.getMessages(getMessageCommand.url, getMessageCommand.headers).pipeTo(sender())
    case messageAck: MessageAck =>
      //      println("Ack Successful")
      swarmMessageClient.ackMessage(messageAck.url, messageAck.packageId, messageAck.headers)
    case getCookies: SwarmLogin =>
      swarmMessageClient.login(getCookies.url, getCookies.loginCredentials).pipeTo(sender())
    case GetEmailOrPhoneFromDeviceId(deviceId: Long) =>
      val res: Future[Response] = getEmailOrPhoneFromDeviceId(deviceId)
      res.pipeTo(sender())
    case _ =>
      println("Unhandled request") //TODO REPLACE IT WITH LOGGER
  }

  def getEmailOrPhoneFromDeviceId(deviceId: Long):Future[Response]= {
    userDAO.getEmailOrPhoneFromDeviceId(deviceId).map{
      case (phone, email) => GetPhoneOrEmailSuccess(phone, email)
    }
  }
}
