package ca.pigscanfly

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import ca.pigscanfly.httpClient.HttpClient
import ca.pigscanfly.models.MessagePost.encoder
import ca.pigscanfly.models.MessageRetrieval._
import ca.pigscanfly.models.{Message, MessageDelivery, MessagePost, MessageRetrieval}
import io.circe.parser
import io.circe.syntax._

import scala.concurrent.{ExecutionContext, Future}

trait SwarmMessageClient {
  this: HttpClient =>
  implicit def actorSystem: ActorSystem

  implicit def executionContext: ExecutionContext

  def getMessages(url: String, headers: List[HttpHeader]): Future[MessageRetrieval] = {
    for {
      response <- sendRequest(HttpRequest(uri = url, headers = headers))
      messagesString <- Unmarshal(response.entity).to[String]
      messages <- parser.decode[List[Message]](messagesString) match {
        case Right(messages) =>
          Future(messages)
        case Left(err) => Future.failed(err)
      }
    } yield MessageRetrieval(messages)
  }

  def sendMessage(url: String, msg: MessagePost, headers: List[HttpHeader]): Future[MessageDelivery] = {
    for {
      response <- sendRequest(HttpRequest(
        method = HttpMethods.POST,
        uri = url,
        headers = headers,
        entity = HttpEntity(ContentTypes.`application/json`, msg.asJson.toString())
      ))
      encodedResponse <- Unmarshal(response.entity).to[String]
      response <- parser.decode[MessageDelivery](encodedResponse) match {
        case Right(response) =>
          Future(response)
        case Left(err) => Future.failed(err)
      }
    } yield MessageDelivery(response.packetId, response.status)
  }

  def ackMessage(url: String, packetId: Int, headers: List[HttpHeader]): Future[MessageDelivery] = {
    val urlFull = url + "/" + packetId
    for {
      response <- sendRequest(HttpRequest(
        method = HttpMethods.POST,
        uri = urlFull,
        headers = headers
      ))
      ackString <- Unmarshal(response.entity).to[String]
      ackResponse <- parser.decode[MessageDelivery](ackString) match {
        case Right(ackResponse) =>
          Future(ackResponse)
        case Left(err) => Future.failed(err)
      }
    } yield MessageDelivery(ackResponse.packetId, ackResponse.status)
  }
}

