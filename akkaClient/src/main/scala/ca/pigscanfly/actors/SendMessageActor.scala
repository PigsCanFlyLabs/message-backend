package ca.pigscanfly.actors

import akka.actor.{Actor, Props}
import akka.http.scaladsl.model.HttpHeader
import ca.pigscanfly.SwarmStart.swarmMessageClient
import ca.pigscanfly.actors.SendMessageActor.PostMessageCommand
import ca.pigscanfly.models.MessagePost

object SendMessageActor {

  def props: Props = Props(new SendMessageActor)

  sealed trait Command

  case class PostMessageCommand(url: String, message: MessagePost, headers: List[HttpHeader]) extends Command
}

class SendMessageActor extends Actor {
  override def receive: Receive = {
    case postMessage: PostMessageCommand =>
      swarmMessageClient.sendMessage(postMessage.url, postMessage.message, postMessage.headers)
    case _ =>
      println("Unhandled request") //TODO REPLACE IT WITH LOGGER
  }
}
