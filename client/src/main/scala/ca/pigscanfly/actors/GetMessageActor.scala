package ca.pigscanfly.actors

import akka.actor.{Actor, Props}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpHeader, HttpMethods}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.pattern.pipe
import ca.pigscanfly.SwarmStart.system
import ca.pigscanfly.actors.GetMessageActor.{GetMessage, MessageAck}
import ca.pigscanfly.configs.Constants
import ca.pigscanfly.httpClient.HttpClient
import ca.pigscanfly.models.{Message, MessageDelivery, MessageRetrieval}

import scala.concurrent.ExecutionContext.Implicits.global


object GetMessageActor {

  def props: Props = Props(new GetMessageActor)

  sealed trait Command

  case class GetMessage(url: String, headers: List[HttpHeader]) extends Command

  case class MessageAck(url: String, packageId: Int, headers: List[HttpHeader]) extends Command
}

class GetMessageActor extends Actor with HttpClient with SprayJsonSupport {
  override def receive: Receive = {
    case getMessageCommand: GetMessage =>
      //      Future(MessageRetrieval(List(Message(1,1,1,"sdfasf",1,1,1,"sfdasdfa",1,1,"strkngr")))).pipeTo(sender())

      val response = sendRequest(getMessageCommand.url, getMessageCommand.headers, Constants.EmptyString, HttpMethods.GET).flatMap { response =>
        Unmarshal(response.entity).to[List[Message]].map { messages =>
          messages.map { message => message.copy(data = new String(java.util.Base64.getDecoder.decode(message.data))) }
          MessageRetrieval(messages)
        }
      }
      response.pipeTo(sender())

    //      swarmMessageClient.getMessages(getMessageCommand.url, getMessageCommand.headers).pipeTo(sender())
    case messageAck: MessageAck =>
      println("Ack Successful")
      val completeUrl = messageAck.url + "/" + messageAck.packageId
      sendRequest(completeUrl, messageAck.headers, Constants.EmptyString, HttpMethods.POST).flatMap { response =>
        Unmarshal(response.entity).to[MessageDelivery]
      }
    //      swarmMessageClient.ackMessage(messageAck.url, messageAck.packageId, messageAck.headers)
    case _ =>
      println("Unhandled request") //TODO REPLACE IT WITH LOGGER
  }
}
