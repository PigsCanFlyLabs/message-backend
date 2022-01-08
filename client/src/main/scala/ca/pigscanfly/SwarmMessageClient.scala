package ca.pigscanfly

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import ca.pigscanfly.configs.Constants
import ca.pigscanfly.httpClient.HttpClient2
import ca.pigscanfly.models.MessagePost.encoder
import ca.pigscanfly.models.MessageRetrieval._
import ca.pigscanfly.models.{Message, MessageDelivery, MessagePost, MessageRetrieval}
import io.circe.parser
import io.circe.syntax._

import scala.concurrent.{ExecutionContext, Future}


trait SwarmMessageClient extends SprayJsonSupport with HttpClient2 {

  implicit def actorSystem: ActorSystem

  implicit def executionContext: ExecutionContext

  def getMessages(url: String, headers: List[HttpHeader]): Future[MessageRetrieval] = {
    sendRequest2(url, headers, Constants.EmptyString, HttpMethods.GET).flatMap { response =>
      Unmarshal(response.entity).to[List[Message]].map { messages =>
        messages.map { message => message.copy(data = new String(java.util.Base64.getDecoder.decode(message.data))) }
        MessageRetrieval(messages)
      }
    }
  }

  //    for {
  //      response <- sendRequest(HttpRequest(uri = url, headers = headers))
  //      messagesString <- Unmarshal(response.entity).to[String]
  //      messages <- parser.decode[List[Message]](messagesString) match {
  //        case Right(messages) =>
  //          Future(messages.map(message => message.copy(data = new String(java.util.Base64.getDecoder.decode(message.data)))))
  //        case Left(err) => Future.failed(err)
  //      }
  //    } yield MessageRetrieval(messages)
  //  }

  def sendMessage(url: String, msg: MessagePost, headers: List[HttpHeader]): Future[MessageDelivery] = {
    sendRequest2(url, headers, msg.asJson.toString(), HttpMethods.POST).flatMap { response =>
      Unmarshal(response.entity).to[MessageDelivery]
    }

    //    for {
    //      response <- sendRequest(HttpRequest(
    //        method = HttpMethods.POST,
    //        uri = url,
    //        headers = headers,
    //        entity = HttpEntity(ContentTypes.`application/json`, msg.asJson.toString())
    //      ))
    //      encodedResponse <- Unmarshal(response.entity).to[String]
    //      response <- parser.decode[MessageDelivery](encodedResponse) match {
    //        case Right(response) =>
    //          Future(response)
    //        case Left(err) => Future.failed(err)
    //      }
    //    } yield
    //    MessageDelivery(response.packetId, response.status)
  }

  def ackMessage(url: String, packetId: Int, headers: List[HttpHeader]): Future[MessageDelivery] = {
    val completeUrl = url + "/" + packetId
    sendRequest2(completeUrl, headers, Constants.EmptyString, HttpMethods.POST).flatMap { response =>
      Unmarshal(response.entity).to[MessageDelivery]
    }

    //    val urlFull = url + "/" + packetId
    //    for {
    //      response <- sendRequest(HttpRequest(
    //        method = HttpMethods.POST,
    //        uri = urlFull,
    //        headers = headers
    //      ))
    //      ackString <- Unmarshal(response.entity).to[String]
    //      ackResponse <- parser.decode[MessageDelivery](ackString) match {
    //        case Right(ackResponse) =>
    //          Future(ackResponse)
    //        case Left(err) => Future.failed(err)
    //      }
    //    } yield MessageDelivery(ackResponse.packetId, ackResponse.status)
  }
}

