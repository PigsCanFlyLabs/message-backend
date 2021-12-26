package ca.pigscanfly

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import ca.pigscanfly.httpClient.HttpClient
import ca.pigscanfly.models.MessageRetrieval
import ca.pigscanfly.models.MessageRetrieval._
import io.circe.parser

import scala.concurrent.{ExecutionContext, Future}

trait SwarmMessageClient {
  this: HttpClient =>
  implicit def actorSystem: ActorSystem

  implicit def executionContext: ExecutionContext

  def getMessages(url: String): Future[MessageRetrieval] = {
    val cookieHeader = akka.http.scaladsl.model.headers.Cookie("JSESSIONID", "B120DCEBC05C9F6CE3FBCA259356C17E")
    for {
      response <- sendRequest(HttpRequest(uri = url, headers = List(cookieHeader)))
      messagesString <- Unmarshal(response.entity).to[String]
      messages <- parser.decode[MessageRetrieval](messagesString) match {
        case Right(messages) => Future(messages)
        case Left(err) => Future.failed(err)
      }
    } yield messages
  }
}

