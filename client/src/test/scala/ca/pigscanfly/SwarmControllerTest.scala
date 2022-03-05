package ca.pigscanfly

import akka.actor.ActorSystem
import akka.http.scaladsl.testkit.ScalatestRouteTest
import ca.pigscanfly.controllers.SwarmController
import ca.pigscanfly.dao.UserDAO
import ca.pigscanfly.models.MessageRetrieval
import ca.pigscanfly.service.{SwarmService, TwilioService}
import org.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

class SwarmControllerTest extends WordSpec with Matchers with ScalatestRouteTest
  with MockitoSugar {

  implicit val twilioService = mock[TwilioService]

  implicit val userDAO = mock[UserDAO]
  implicit val actor: ActorSystem = ActorSystem("Swarm-Start")
  val swarmService = mock[SwarmService]


  val swarmController = mock[SwarmController]
  val route = swarmController.routes


  "return OK for get messages" in {
    Get("/messages") ~> route ~> check {
      swarmService.getMessages().map { x =>
        assert(x == MessageRetrieval(List()))
      }
    }
  }

}
