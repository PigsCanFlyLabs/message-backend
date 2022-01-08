package ca.pigscanfly

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import ca.pigscanfly.configs.Constants
import ca.pigscanfly.httpClient.HttpClient
import ca.pigscanfly.models.MessagePost.encoder
import ca.pigscanfly.models.MessageRetrieval._
import ca.pigscanfly.models.{Message, MessageDelivery, MessagePost, MessageRetrieval}
import io.circe.syntax._

import scala.concurrent.{ExecutionContext, Future}


trait SwarmMessageClient extends SprayJsonSupport with HttpClient {

  implicit def actorSystem: ActorSystem

  implicit def executionContext: ExecutionContext

  def getMessages(url: String, headers: List[HttpHeader]): Future[MessageRetrieval] = {
    sendRequest(url, headers, Constants.EmptyString, HttpMethods.GET).flatMap { response =>
      Unmarshal(response.entity).to[List[Message]].map { messages =>
        val updatedMessages = messages.map { message => message.copy(data = new String(java.util.Base64.getDecoder.decode(message.data))) }
        MessageRetrieval(updatedMessages)
      }
    }
  }

  def sendMessage(url: String, msg: MessagePost, headers: List[HttpHeader]): Future[MessageDelivery] = {
    sendRequest(url, headers, msg.asJson.toString(), HttpMethods.POST).flatMap { response =>
      Unmarshal(response.entity).to[MessageDelivery]
    }
  }

  def ackMessage(url: String, packetId: Int, headers: List[HttpHeader]): Future[MessageDelivery] = {
    val completeUrl = url + "/" + packetId
    sendRequest(completeUrl, headers, Constants.EmptyString, HttpMethods.POST).flatMap { response =>
      Unmarshal(response.entity).to[MessageDelivery]
    }
  }
}

