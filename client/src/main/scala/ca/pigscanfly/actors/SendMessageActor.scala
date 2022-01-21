package ca.pigscanfly.actors

import akka.actor.{Actor, Props}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.HttpHeader
import akka.pattern.pipe
import ca.pigscanfly.Application.swarmMessageClient
import ca.pigscanfly.actors.SendMessageActor._
import ca.pigscanfly.dao.UserDAO
import ca.pigscanfly.httpClient.HttpClient
import ca.pigscanfly.models.MessagePost

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SendMessageActor {

  def props(userDAO:UserDAO): Props = Props(new SendMessageActor(userDAO))

  sealed trait Command
  sealed trait Response

  case class PostMessageCommand(url: String, message: MessagePost, headers: List[HttpHeader]) extends Command

  case class GetDeviceIdFromEmailOrPhone(from:String) extends Command
  case class GetDeviceIdSuccess(deviceId:Option[Long]) extends Response
}

class SendMessageActor(userDAO:UserDAO) extends Actor with HttpClient with SprayJsonSupport {
  override def receive: Receive = {
    case GetDeviceIdFromEmailOrPhone(from:String) =>
     val res= getDeviceIdFromEmailOrPhone(from)
     res.pipeTo(sender())
    case postMessage: PostMessageCommand =>
      swarmMessageClient.sendMessage(postMessage.url, postMessage.message, postMessage.headers).pipeTo(sender())
    case _ =>
      println("Unhandled request") //TODO REPLACE IT WITH LOGGER
  }


  def getDeviceIdFromEmailOrPhone(from:String): Future[Response] ={
    userDAO.getDeviceIdFromEmailOrPhone(from).map{
      case Some(deviceId)=>GetDeviceIdSuccess(Some(deviceId))
      case None=>GetDeviceIdSuccess(None)
    }
  }
}
