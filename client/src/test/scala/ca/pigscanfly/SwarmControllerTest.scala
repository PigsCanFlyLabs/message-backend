package ca.pigscanfly

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.{ContentTypes, FormData, HttpEntity, Multipart, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import ca.pigscanfly.Application.{swarmMessageClient, system, userDAO}
import ca.pigscanfly.actors.{GetMessageActor, SendMessageActor}
import ca.pigscanfly.controllers.SwarmController
import ca.pigscanfly.dao.UserDAO
import ca.pigscanfly.models.{MessageDelivery, MessageRetrieval, ScheduleSendMessageRequest}
import ca.pigscanfly.schedular.SendMessageManager
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
  val sendMessageManager: ActorRef = system.actorOf(Props(new SendMessageManager(swarmService, sendMessageActor, getMessageActor)))


  implicit val swarmController: SwarmController = new SwarmController(swarmService, sendMessageActor, getMessageActor, sendMessageManager)
  val route: Route = swarmController.routes


  "return OK for get messages" in {
    when(swarmService.getMessages(getMessageActor)) thenReturn (Future.successful(MessageRetrieval(List())))
    Get("/messages") ~> route ~> check {
      status shouldEqual StatusCodes.OK
    }
  }

  "return OK for Post messages" in {
    val respone=ScheduleSendMessageRequest("cysomerId",0L,"reciever","message")
    when(swarmService.postMessages("sender@domain.com", "reciever@domain.com",
      "message", sendMessageActor, getMessageActor, sendMessageManager)) thenReturn Future.successful(respone)
    val request = FormData("From" -> "sender@domain.com", "To" -> "reciever@domain.com", "Body" -> "message")
    Post("/messages", request) ~> route ~> check {
      status shouldEqual StatusCodes.OK
    }
  }

}
