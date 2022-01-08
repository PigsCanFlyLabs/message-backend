package ca.pigscanfly.httpClient

import akka.actor.ActorSystem
import akka.http.javadsl.model.HttpMethods
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._

import scala.concurrent.Future


trait HttpClient {
  def sendRequest(finalApiPath: String, headers: List[HttpHeader], message: String, requestMethod: HttpMethod)(implicit actorSystem: ActorSystem): Future[HttpResponse] = {
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
