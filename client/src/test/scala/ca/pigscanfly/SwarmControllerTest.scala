package ca.pigscanfly

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.{ContentTypes, FormData, HttpEntity, Multipart, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import ca.pigscanfly.Application.{swarmMessageClient, system, userDAO}
import ca.pigscanfly.actors.{GetMessageActor, SendMessageActor}
import ca.pigscanfly.controllers.SwarmController
import ca.pigscanfly.dao.UserDAO
import ca.pigscanfly.models.{MessageDelivery, MessageRetrieval}
import ca.pigscanfly.service.{SwarmService, TwilioService}
import org.mockito.MockitoSugar
import org.mockito.MockitoSugar.mock
import org.scalatest.{Matchers, WordSpec, WordSpecLike}
import io.circe.syntax._

import scala.concurrent.Future

class SwarmControllerTest extends WordSpec with Matchers with ScalatestRouteTest
  with MockitoSugar {

  implicit val twilioService: TwilioService = mock[TwilioService]

  val userDAO: UserDAO = mock[UserDAO]
  val actor: ActorSystem = ActorSystem("Swarm-Start")
  val swarmService: SwarmService = mock[SwarmService]
  val sendMessageActor: ActorRef = system.actorOf(SendMessageActor.props(userDAO, swarmMessageClient))
  val getMessageActor: ActorRef = system.actorOf(GetMessageActor.props(userDAO, swarmMessageClient))


  implicit val swarmController: SwarmController = new SwarmController(swarmService, sendMessageActor, getMessageActor)
  val route: Route = swarmController.routes


  "return OK for get messages" in {
    when(swarmService.getMessages(getMessageActor)) thenReturn (Future.successful(MessageRetrieval(List())))
    Get("/messages") ~> route ~> check {
      status shouldEqual StatusCodes.OK
    }
  }

  "return OK for Post messages" in {
    when(swarmService.postMessages("sender@domain.com", "reciever@domain.com",
      "message", sendMessageActor, getMessageActor)) thenReturn Future.successful(MessageDelivery(1, "pending"))
    val request = FormData("From" -> "sender@domain.com", "To" -> "reciever@domain.com", "Body" -> "message")
    Post("/messages", request) ~> route ~> check {
      status shouldEqual StatusCodes.OK
    }
  }

}
