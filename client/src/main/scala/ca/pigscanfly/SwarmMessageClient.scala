package ca.pigscanfly

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import ca.pigscanfly.configs.Constants
import ca.pigscanfly.httpClient.HttpClient
import ca.pigscanfly.models.MessageRetrieval._
import ca.pigscanfly.models.{Login, LoginCredentials, GetMessage, MessageDelivery, MessagePost, MessageRetrieval}
import ca.pigscanfly.util.ProtoUtils
import io.circe.syntax._

import scala.concurrent.{ExecutionContext, Future}


trait SwarmMessageClient extends SprayJsonSupport with HttpClient with ProtoUtils {

  implicit def actorSystem: ActorSystem

  implicit def executionContext: ExecutionContext

  def login(url: String, loginCredentials: LoginCredentials): Future[Seq[HttpHeader]] = {
    sendRequest(url, List.empty, loginCredentials.asJson.toString(), HttpMethods.GET).map { response =>
      response.headers
    }
  }

  def getMessages(url: String, headers: List[HttpHeader]): Future[MessageRetrieval] = {
    sendRequest(url, headers, Constants.EmptyString, HttpMethods.GET).flatMap { response =>
      Unmarshal(response.entity).to[List[GetMessage]].map { messages =>
        val updatedMessages = messages.map { message => message/*.copy(data = decodeMessage(message.data).data)*/ }
        MessageRetrieval(updatedMessages)
      }
    }
  }

  def sendMessage(url: String, msg: MessagePost, headers: List[HttpHeader]): Future[MessageDelivery] = {
    sendRequest(url, headers, msg.copy(data = msg.data).asJson.toString(), HttpMethods.POST).flatMap { response =>
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

