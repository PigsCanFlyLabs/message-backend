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
   * This method is responsible Swarm Satellite's authentication i.e. to logged in the swarm satellite
   *
   * @param url              : Swam Satellite's request url
   * @param loginCredentials : request contains swarm's username and password
   * @return return headers after successful login
   */
  def login(url: String, loginCredentials: LoginCredentials): Future[Seq[HttpHeader]] = {
    sendRequest(url, List.empty, loginCredentials.asJson.toString(), HttpMethods.GET).map { response =>
      response.headers
    }
  }

  /**
   * This method is responsible to retrieve messages from the Swarm Satellite
   *
   * @param url     : Swam Satellite's request url
   * @param headers : these are retrieved from Swarm Satellite's successful login
   * @return List[GetMessage]:
   *         GetMessage contains (packetId, deviceType, deviceId, deviceName, dataType, userApplicationId, len, data, ackPacketId, status, hiveRxTime) of the  retrieved message
   */
  def getMessages(url: String, headers: List[HttpHeader]): Future[MessageRetrieval] = {
    sendRequest(url, headers, Constants.EmptyString, HttpMethods.GET).flatMap { response =>
      Unmarshal(response.entity).to[List[GetMessage]].map { messages =>
        MessageRetrieval(messages)
      }
    }
  }

  /**
   * This method is responsible to send message to Swarm Satellite using it's request url and message details i.e.
   * type of device to which message is to be sent, device id, message and user's application id
   *
   * @param url     : Swam Satellite's request url
   * @param msg     : request contains device_type, device_id, user_application_id, message
   * @param headers :these are retrieved from Swarm Satellite's successful login
   * @return Future[MessageDelivery]
   *         MessageDelivery: contains packetId and status of the sent message
   */
  def sendMessage(url: String, msg: MessagePost, headers: List[HttpHeader]): Future[MessageDelivery] = {
    sendRequest(url, headers, msg.copy(data = msg.data).asJson.toString(), HttpMethods.POST).flatMap { response =>
      Unmarshal(response.entity).to[MessageDelivery]
    }
  }

  /**
   * This method is responsible to send acknowledgement to Swarm Satellite for the messages that have been retrieved from Swarm Satelite
   *
   * @param url      : Swam Satellite's request url
   * @param packetId : unique identifier of the message for which acknowledgement is to be sent
   * @param headers  :these are retrieved from Swarm Satellite's successful login
   * @return Future[MessageDelivery]
   *         MessageDelivery: contains packetId and status of the sent message
   */
  def ackMessage(url: String, packetId: Int, headers: List[HttpHeader]): Future[MessageDelivery] = {
    val completeUrl = url + "/" + packetId
    sendRequest(completeUrl, headers, Constants.EmptyString, HttpMethods.POST).flatMap { response =>
      Unmarshal(response.entity).to[MessageDelivery]
    }
  }
}

