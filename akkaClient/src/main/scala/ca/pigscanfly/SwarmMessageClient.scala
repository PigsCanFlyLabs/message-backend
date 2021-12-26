package ca.pigscanfly

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import ca.pigscanfly.httpClient.HttpClient
import ca.pigscanfly.models.{Message, MessageRetrieval}
import ca.pigscanfly.models.MessageRetrieval._
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
}

