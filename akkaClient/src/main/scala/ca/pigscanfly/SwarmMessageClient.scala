package ca.pigscanfly

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import ca.pigscanfly.httpClient.HttpClient
import ca.pigscanfly.models.{Message, MessageDelivery, MessagePost, MessageRetrieval}
import ca.pigscanfly.models.MessageRetrieval._
import ca.pigscanfly.models.MessagePost.encoder
import io.circe.parser
import akka.http.scaladsl.model.headers.Cookie

import scala.concurrent.{ExecutionContext, Future}

trait SwarmMessageClient { this: HttpClient =>
  implicit def actorSystem: ActorSystem

  implicit def executionContext: ExecutionContext

  def getMessages(url: String): Future[MessageRetrieval] = {
    val cookieHeader = Cookie("JSESSIONID", "B120DCEBC05C9F6CE3FBCA259356C17E")
    for {
      response <- sendRequest(HttpRequest(uri = url, headers = List(cookieHeader)))
      messagesString <- Unmarshal(response.entity).to[String]
      messages <- parser.decode[List[Message]](messagesString) match {
        case Right(messages) =>
          Future(messages)
        case Left(err) => Future.failed(err)
      }
    } yield MessageRetrieval(messages)
  }

  def postMessage(url: String, msg: MessagePost): Future[MessageDelivery] = {
    val cookieHeader = Cookie("JSESSIONID", "B120DCEBC05C9F6CE3FBCA259356C17E")
    val req = encoder.apply(msg).toString()
    for {
      response <- sendRequest(HttpRequest(
        method = HttpMethods.POST,
        uri = url,
        headers = List(cookieHeader),
        entity = HttpEntity(ContentTypes.`application/json`, req)
      ))
      encodedResponse <- Unmarshal(response.entity).to[String]
      response <- parser.decode[MessageDelivery](encodedResponse) match {
        case Right(response) =>
          Future(response)
        case Left(err) => Future.failed(err)
      }
    } yield MessageDelivery(response.packetId, response.status)
  }

  def ackMessage(url: String, packetId: Int): Future[MessageDelivery] = {
    val cookieHeader = Cookie("JSESSIONID", "B120DCEBC05C9F6CE3FBCA259356C17E")
    val urlFull = url + "/" + packetId
    for {
      response <- sendRequest(HttpRequest(
        method = HttpMethods.POST,
        uri = urlFull,
        headers = List(cookieHeader)
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

