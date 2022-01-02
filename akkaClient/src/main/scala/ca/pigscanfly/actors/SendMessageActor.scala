package ca.pigscanfly.actors

import akka.actor.{Actor, Props}
import akka.http.scaladsl.model.HttpHeader
import ca.pigscanfly.SwarmStart.newMessage
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
      newMessage.sendMessage(postMessage.url, postMessage.message, postMessage.headers)
  }
}
