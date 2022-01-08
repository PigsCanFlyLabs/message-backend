package ca.pigscanfly.actors

import akka.actor.{Actor, Props}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpHeader, HttpMethods}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.pattern.pipe
import ca.pigscanfly.SwarmStart.system
import ca.pigscanfly.actors.SendMessageActor.PostMessageCommand
import ca.pigscanfly.httpClient.HttpClient
import ca.pigscanfly.models.{MessageDelivery, MessagePost}
import io.circe.syntax.EncoderOps

import scala.concurrent.ExecutionContext.Implicits.global

object SendMessageActor {

  def props: Props = Props(new SendMessageActor)

  sealed trait Command

  case class PostMessageCommand(url: String, message: MessagePost, headers: List[HttpHeader]) extends Command
}

class SendMessageActor extends Actor with HttpClient with SprayJsonSupport {
  override def receive: Receive = {
    case postMessage: PostMessageCommand =>
      sendRequest(postMessage.url, postMessage.headers, postMessage.message.asJson.toString(), HttpMethods.POST).flatMap { response =>
        Unmarshal(response.entity).to[MessageDelivery]
      }.pipeTo(sender())
//      swarmMessageClient.sendMessage(postMessage.url, postMessage.message, postMessage.headers).pipeTo(sender())
    case _ =>
      println("Unhandled request") //TODO REPLACE IT WITH LOGGER
  }
}
