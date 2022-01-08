package ca.pigscanfly.actors

import akka.actor.{Actor, Props}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.HttpHeader
import akka.pattern.pipe
import ca.pigscanfly.Application.{executionContext, swarmMessageClient}
import ca.pigscanfly.actors.GetMessageActor.{GetMessage, MessageAck}
import ca.pigscanfly.httpClient.HttpClient


object GetMessageActor {

  def props: Props = Props(new GetMessageActor)

  sealed trait Command

  case class GetMessage(url: String, headers: List[HttpHeader]) extends Command

  case class MessageAck(url: String, packageId: Int, headers: List[HttpHeader]) extends Command
}

class GetMessageActor extends Actor with HttpClient with SprayJsonSupport {
  override def receive: Receive = {
    case getMessageCommand: GetMessage =>
      //            Future(MessageRetrieval(List(Message(1,1,1,"sdfasf",1,1,1,"sfdasdfa",1,1,"strkngr")))).pipeTo(sender())
      swarmMessageClient.getMessages(getMessageCommand.url, getMessageCommand.headers).pipeTo(sender())

    case messageAck: MessageAck =>
      //      println("Ack Successful")
      swarmMessageClient.ackMessage(messageAck.url, messageAck.packageId, messageAck.headers)
    case _ =>
      println("Unhandled request") //TODO REPLACE IT WITH LOGGER
  }
}
