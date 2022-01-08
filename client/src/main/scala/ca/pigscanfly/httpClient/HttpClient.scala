package ca.pigscanfly.httpClient

import akka.actor.ActorSystem
import akka.http.javadsl.model.HttpMethods
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpHeader, HttpMethod, HttpRequest, HttpResponse}
import org.apache.http.HttpHeaders

import scala.concurrent.Future

//trait HttpClient {
//  def sendRequest(httpRequest: HttpRequest)(implicit actorSystem: ActorSystem): Future[HttpResponse]
//}

trait HttpClient2  {
  def sendRequest2(finalApiPath: String, headers: List[HttpHeader], message: String, requestMethod: HttpMethod)(implicit actorSystem: ActorSystem): Future[HttpResponse] = {
    val requestEntity = if (requestMethod == HttpMethods.GET) HttpEntity.Empty else HttpEntity(ContentTypes.`application/json`, message)
    val request = HttpRequest(
      method = requestMethod,
      uri = finalApiPath,
      entity = requestEntity,
      headers = headers
    )
    Http().singleRequest(request)

  }
}

//trait ClientHandler extends HttpClient {
//  override def sendRequest(httpRequest: HttpRequest)(implicit actorSystem: ActorSystem): Future[HttpResponse] = {
//    Http().singleRequest(httpRequest)
//  }
//
//  def shutDown()(implicit actorSystem: ActorSystem): Unit = {
//    Http().shutdownAllConnectionPools()
//  }
//}
