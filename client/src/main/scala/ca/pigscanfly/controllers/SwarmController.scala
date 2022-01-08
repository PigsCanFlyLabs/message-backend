package ca.pigscanfly.controllers

import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ca.pigscanfly.service.SwarmService
import io.circe.syntax._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class SwarmController(swarmService: SwarmService) {

  //  val getMessageActor: ActorRef = actorSystem.actorOf(GetMessageActor.props)
  //  val sendMessageActor: ActorRef = actorSystem.actorOf(SendMessageActor.props)

  //  implicit val timeout = Timeout(3.seconds)

  def routes: Route = path("messages") {
    get {
      extractRequest { request =>
        val result = swarmService.getMessages(request)
        onComplete(result) {
          case Success(messages) => complete(HttpEntity(ContentTypes.`application/json`, messages.asJson.toString))
          case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
        }

//          .map { response => {
//          HttpResponse(
//            status = StatusCodes.OK,
//            entity = HttpEntity(ContentTypes.`application/json`, response.asJson.toString))
//        }
//        }.recover {
//          //TODO: Create Function to throw error response
//          case ex =>
//            HttpResponse(
//              status = StatusCodes.InternalServerError,
//              entity = HttpEntity(ContentTypes.`application/json`, s"An error occurred: ${ex.getMessage}")
//            )
//        }
//        complete(result)
//                val cookies = req.cookies.map { cookie =>
//                  Cookie(cookie.name, cookie.value)
//                }
//                val messagesFut = (getMessageActor ? GetMessage(s"$SwarmBaseUrl/hive/api/v1/messages", cookies.toList)).mapTo[MessageRetrieval]
//
//                messagesFut.map { messages =>
//                  messages.messageResponse.map { message =>
//                    getMessageActor ! MessageAck(s"$SwarmBaseUrl/hive/api/v1/messages/rxack", message.ackPacketId, cookies.toList)
//                    twilioService.sendToTwilio(Constants.EmptyString, Constants.EmptyString, message.data)
//                  }
//                }
//                onComplete(messagesFut) {
//                  case Success(messages) => complete(HttpEntity(ContentTypes.`application/json`, messages.asJson.toString))
//                  case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
//                }

      }
    }
  }
  //  ~ path("send/messages") {
  //    post {
  //      entity(as[MessagePost]) { messagePost =>
  //        extractRequest { req =>
  //          val cookies = req.cookies.map { cookie =>
  //            Cookie(cookie.name, cookie.value)
  //          }
  //          val responseFut = (sendMessageActor ? PostMessageCommand(s"$SwarmBaseUrl/hive/api/v1/messages", messagePost.copy(data = java.util.Base64.getEncoder.encodeToString(messagePost.data.getBytes())), cookies.toList)).mapTo[MessageDelivery].map(_.asJson)
  //          onComplete(responseFut) {
  //            case Success(response) => complete(HttpEntity(ContentTypes.`application/json`, response.toString))
  //            case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
  //          }
  //
  //        }
  //      }
  //    }
  //  }
}
