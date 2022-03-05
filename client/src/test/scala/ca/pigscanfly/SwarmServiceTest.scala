package ca.pigscanfly

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.http.scaladsl.model.{HttpHeader, HttpMethod, HttpResponse}
import akka.testkit.TestActorRef
import ca.pigscanfly.actors.GetMessageActor.{GetEmailOrPhoneFromDeviceId, GetPhoneOrEmailSuccess, SwarmLogin}
import ca.pigscanfly.dao.UserDAO
import ca.pigscanfly.httpClient.HttpClient
import ca.pigscanfly.models.LoginCredentials
import ca.pigscanfly.service.{SwarmService, TwilioService}
import org.mockito.MockitoSugar.mock
import org.scalamock.scalatest.MockFactory
import org.scalatest.WordSpecLike

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

class SwarmServiceTest extends SwarmService(mock[TwilioService])(ActorSystem(), mock[UserDAO])
  with WordSpecLike with MockFactory {

  implicit val userDAO: UserDAO = mock[UserDAO]

  implicit val system: ActorSystem = ActorSystem()
  override val getMessageActor: ActorRef = TestActorRef(new Actor {
    def receive: Receive = {
      case GetEmailOrPhoneFromDeviceId(0L) ⇒
        sender ! GetPhoneOrEmailSuccess(None, None)
      case SwarmLogin =>
        sender ! Future.successful(Seq())

    }
  })
  val swarmMessageClient = new SwarmMessageClient with MockClientHandler {
    override implicit def actorSystem: ActorSystem = system

    override implicit def executionContext: ExecutionContext = ExecutionContext.Implicits.global
  }
  val loginCredentials = LoginCredentials("username", "password")

  trait MockClientHandler extends HttpClient {
    val mock = mockFunction[(String, List[HttpHeader], String, HttpMethod), Future[HttpResponse]]

    override def sendRequest(finalApiPath: String, headers: List[HttpHeader], message: String, requestMethod: HttpMethod)(implicit actorSystem: ActorSystem): Future[HttpResponse] =
      mock(finalApiPath, headers, message, requestMethod)
  }


  "SwarmService" should {

    "able to get Phone Or Email From Device Id" in {
      val result = Await.result(getPhoneOrEmailFromDeviceId(0L), 5 second)
      assert(result.email.isEmpty)
    }

    "able to swarmLogin" in {
      val result = Await.result(swarmLogin(loginCredentials), 5 second)

      assert(result.isEmpty)
    }
  }

}
