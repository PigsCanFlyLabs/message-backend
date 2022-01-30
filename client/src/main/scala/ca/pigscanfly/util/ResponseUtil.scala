package ca.pigscanfly.util

import akka.http.scaladsl.model._

object ResponseUtil {

  def prepareSuccessResponse[T](result: T): Unit = {

    //    HttpResponse(status = StatusCodes.OK,
    //      entity = HttpEntity(ContentTypes.`application/json`, result.asJson.toString))
  }

  def prepareErrorResponse(status: StatusCode, exception: String): Unit = {

    //    HttpResponse(status = status,
    //      entity = HttpEntity(ContentTypes.`application/json`, exception))
  }

}
