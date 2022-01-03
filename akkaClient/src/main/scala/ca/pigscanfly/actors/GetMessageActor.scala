package ca.pigscanfly.actors

import akka.actor.{Actor, Props}
import akka.http.scaladsl.model.HttpHeader
import ca.pigscanfly.SwarmStart.swarmMessageClient
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
      swarmMessageClient.getMessages(getMessageCommand.url, getMessageCommand.headers)
    case messageAck: MessageAck =>
      swarmMessageClient.ackMessage(messageAck.url, messageAck.packageId, messageAck.headers)
    case _ =>
      println("Unhandled request") //TODO REPLACE IT WITH
  }
}
