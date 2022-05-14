package ca.pigscanfly

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer.matFromSystem
import ca.pigscanfly.configs.Constants
import ca.pigscanfly.httpClient.HttpClient
import ca.pigscanfly.models.GetMessage._
import ca.pigscanfly.models._
import ca.pigscanfly.util.ProtoUtils
import io.circe.syntax._

import scala.concurrent.{ExecutionContext, Future}

trait SwarmMessageClient extends SprayJsonSupport with HttpClient with ProtoUtils {

  implicit def actorSystem: ActorSystem

  implicit def executionContext: ExecutionContext

  /**
   *
   * @param url
   * @param loginCredentials
   * @return
   */
  def login(url: String, loginCredentials: LoginCredentials): Future[Seq[HttpHeader]] = {
    sendRequest(url, List.empty, loginCredentials.asJson.toString(), HttpMethods.GET).map { response =>
      response.headers
    }
  }

  /**
   *
   * @param url
   * @param headers
   * @return
   */
  def getMessages(url: String, headers: List[HttpHeader]): Future[MessageRetrieval] = {
    sendRequest(url, headers, Constants.EmptyString, HttpMethods.GET).flatMap { response =>
      Unmarshal(response.entity).to[List[GetMessage]].map { messages =>
        MessageRetrieval(messages)
      }
    }
  }

  /**
   *
   * @param url
   * @param msg
   * @param headers
   * @return
   */
  def sendMessage(url: String, msg: MessagePost, headers: List[HttpHeader]): Future[MessageDelivery] = {
    sendRequest(url, headers, msg.copy(data = msg.data).asJson.toString(), HttpMethods.POST).flatMap { response =>
      Unmarshal(response.entity).to[MessageDelivery]
    }
  }

  /**
   *
   * @param url
   * @param packetId
   * @param headers
   * @return
   */
  def ackMessage(url: String, packetId: Int, headers: List[HttpHeader]): Future[MessageDelivery] = {
    val completeUrl = url + "/" + packetId
    sendRequest(completeUrl, headers, Constants.EmptyString, HttpMethods.POST).flatMap { response =>
      Unmarshal(response.entity).to[MessageDelivery]
    }
  }
}

