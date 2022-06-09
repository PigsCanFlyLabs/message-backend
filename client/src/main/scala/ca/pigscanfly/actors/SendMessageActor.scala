package ca.pigscanfly.actors

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.HttpHeader
import akka.pattern.pipe
import ca.pigscanfly.SwarmMessageClient
import ca.pigscanfly.actors.SendMessageActor._
import ca.pigscanfly.components.MessageHistory
import ca.pigscanfly.dao.UserDAO
import ca.pigscanfly.httpClient.HttpClient
import ca.pigscanfly.models.MessagePost

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SendMessageActor {

  def props(userDAO: UserDAO, swarmMessageClient: SwarmMessageClient): Props = Props(new SendMessageActor(userDAO, swarmMessageClient))

  sealed trait Command

  sealed trait Response

  case class CheckSubscription(deviceId: Long) extends Command

  case class GetDeviceIdFromEmailOrPhone(from: String) extends Command

  case class PostMessageCommand(url: String, message: MessagePost, headers: List[HttpHeader]) extends Command

  case class GetDeviceId(deviceId: Option[Long], customerId: Option[String]) extends Response

  case class CheckDeviceSubscription(isDisabled: Option[Boolean], customerId: Option[String]) extends Response

}

class SendMessageActor(userDAO: UserDAO, swarmMessageClient: SwarmMessageClient) extends Actor with HttpClient with SprayJsonSupport with ActorLogging {
  override def receive: Receive = {
    case GetDeviceIdFromEmailOrPhone(from: String) =>
      log.info(s"SendMessageActor: Fetching device from: $from")
      val res: Future[Response] = userDAO.getDeviceIdFromEmailOrPhone(from).map {
        case Some(response) =>
          val (deviceId, customerId) = response
          GetDeviceId(Some(deviceId), customerId)
        case None =>
          GetDeviceId(None, None)
      }
      res.pipeTo(sender())
    case postMessage: PostMessageCommand =>
      log.info(s"SendMessageActor: Sending message to Swarm. url: ${postMessage.url}," +
        s"message: ${postMessage.message}, headers: ${postMessage.headers}")
      swarmMessageClient.sendMessage(postMessage.url, postMessage.message, postMessage.headers).pipeTo(sender())
    case CheckSubscription(deviceId) =>
      log.info(s"SendMessageActor: Checking subscription for deviceId: $deviceId")
      val res: Future[Response] = userDAO.checkUserSubscription(deviceId).map {
        case Some(response) =>
          val (isDisabled, customerId) = response
          CheckDeviceSubscription(Some(isDisabled), customerId)
        case None =>
          CheckDeviceSubscription(None, None)
      }
      res.pipeTo(sender())

    case _ =>
      log.info("SendMessageActor: Received request that has not been handled!")
  }
}
