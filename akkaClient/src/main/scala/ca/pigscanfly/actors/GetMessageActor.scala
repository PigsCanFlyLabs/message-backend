package ca.pigscanfly.actors

import akka.actor.{Actor, Props}
import akka.http.scaladsl.model.HttpHeader
import ca.pigscanfly.SwarmStart.newMessage
import ca.pigscanfly.actors.GetMessageActor.{GetMessage, MessageAck}

object GetMessageActor {

  def props: Props = Props(new GetMessageActor)

  sealed trait Command

  case class GetMessage(url: String, headers: List[HttpHeader]) extends Command

  case class MessageAck(url: String, packageId: Int, headers: List[HttpHeader]) extends Command
}

class GetMessageActor extends Actor {
  override def receive: Receive = {
    case getMessageCommand: GetMessage =>
      newMessage.getMessages(getMessageCommand.url, getMessageCommand.headers)
    case messageAck: MessageAck =>
      newMessage.ackMessage(messageAck.url, messageAck.packageId, messageAck.headers)
  }
}
