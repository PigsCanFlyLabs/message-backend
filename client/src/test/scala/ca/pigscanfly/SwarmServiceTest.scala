package ca.pigscanfly

import akka.actor.{Actor, ActorSystem}
import akka.http.scaladsl.model.headers.{Authorization, Cookie, OAuth2BearerToken, `Content-Type`}
import akka.http.scaladsl.model.{ContentTypes, HttpResponse}
import akka.testkit.TestActorRef
import ca.pigscanfly.actors.GetMessageActor
import ca.pigscanfly.actors.GetMessageActor.{GetEmailOrPhoneFromDeviceId, GetMessage, GetPhoneOrEmailSuccess, MessageAck, SwarmLogin}
import ca.pigscanfly.actors.SendMessageActor.{CheckDeviceSubscription, CheckSubscription, GetDeviceId, GetDeviceIdFromEmailOrPhone, PostMessageCommand}
import ca.pigscanfly.dao.UserDAO
import ca.pigscanfly.models.{LoginCredentials, MessageDelivery, MessageRetrieval}
import ca.pigscanfly.service.{SwarmService, TwilioService}
import org.scalamock.scalatest.MockFactory
import org.scalatest.WordSpecLike

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class SwarmServiceTest extends WordSpecLike with MockFactory {

  val userDAO: UserDAO = mock[UserDAO]
  val loginCredentials: LoginCredentials = LoginCredentials("username", "password")
  val cookieHeader: Cookie = akka.http.scaladsl.model.headers.Cookie("JSESSIONID", "B120DCEBC05C9F6CE3FBCA259356C17E")
  val contentTypeHeader: `Content-Type` = akka.http.scaladsl.model.headers.`Content-Type`(ContentTypes.`application/json`)

  val twilioService: TwilioService = mock[TwilioService]
  val swarmMessageClient: SwarmMessageClient = mock[SwarmMessageClient]
  val swarmService: SwarmService = SwarmService(twilioService)(userDAO, swarmMessageClient)

  implicit val system: ActorSystem = ActorSystem()

  "SwarmService" should {
    "able to getMessages" in {
      val getMessageResponse = ca.pigscanfly.models.GetMessage(packetId = 0,
        deviceType = 1,
        deviceId = 1,
        deviceName = "my device",
        dataType = 2,
        userApplicationId = 3,
        len = 9,
        data = "encoded message",
        ackPacketId = 4,
        status = 1,
        hiveRxTime = "timestamp")
      val auth = Authorization(OAuth2BearerToken("token"))
      val getMessageTestActor = TestActorRef(new Actor {
        def receive: Receive = {
          case SwarmLogin(_, _) =>
            sender() ! HttpResponse.apply().addHeader(auth).headers
          case GetMessage(_, _) =>
            sender() ! MessageRetrieval(List(getMessageResponse))
          case MessageAck(_, _, _) =>
            sender() ! MessageDelivery(1, "pending")
        }
      })
      val result = Await.result(swarmService.getMessages(getMessageTestActor), 5 second)
      assert(result == MessageRetrieval(List(getMessageResponse)))
    }

    "able to get Phone Or Email From Device Id" in {
      val testActor = TestActorRef(new Actor {
        def receive: Receive = {
          case GetEmailOrPhoneFromDeviceId(0L) ⇒
            sender() ! GetPhoneOrEmailSuccess(Some("987653210"), Some("email@domain.com"), Some("customer-id"))
        }
      })
      val result = Await.result(swarmService.getPhoneOrEmailFromDeviceId(0L, testActor), 5 second)
      assert(result == GetPhoneOrEmailSuccess(Some("987653210"), Some("email@domain.com"), Some("customer-id")))
    }

    "not able to get Phone Or Email From Device Id" in {
      val testActor = TestActorRef(new Actor {
        def receive: Receive = {
          case GetEmailOrPhoneFromDeviceId(0L) ⇒
            sender() ! GetPhoneOrEmailSuccess(None, None, None)
        }
      })
      val result = Await.result(swarmService.getPhoneOrEmailFromDeviceId(0L, testActor), 5 second)
      assert(result.email.isEmpty)
    }

    "able to postMessages" in {
      val sendMessageTestActor = TestActorRef(new Actor {
        def receive: Receive = {
          case GetDeviceIdFromEmailOrPhone(_) =>
            sender() ! GetDeviceId(Some(0L), Some("customer-id"))
          case CheckSubscription(_) =>
            sender() ! CheckDeviceSubscription(Some(false), Some("customer-id"))
          case PostMessageCommand(_, _, _) =>
            sender() ! MessageDelivery(0, "pending")
        }
      })
      val getMessageTestActor = TestActorRef(new Actor {
        def receive: Receive = {
          case SwarmLogin(_, _) =>
            sender() ! HttpResponse.apply().headers
        }
      })
      val result = Await.result(swarmService.postMessages("987653210", "8563210", "message", sendMessageTestActor, getMessageTestActor), 5 second)
      assert(result == MessageDelivery(0, "pending"))
    }

    "able to postMessage" in {
      val auth = Authorization(OAuth2BearerToken("token"))
      val testActor = TestActorRef(new Actor {
        def receive: Receive = {
          case SwarmLogin(_, _) =>
            sender() ! HttpResponse.apply().addHeader(auth).headers
        }
      })
      val result = Await.result(swarmService.swarmLogin(loginCredentials, testActor), 5 second)
      assert(result == HttpResponse.apply().addHeader(auth).headers)
    }

    "able to swarmLogin" in {
      val auth = Authorization(OAuth2BearerToken("token"))
      val testActor = TestActorRef(new Actor {
        def receive: Receive = {
          case SwarmLogin(_, _) =>
            sender() ! HttpResponse.apply().addHeader(auth).headers
        }
      })
      val result = Await.result(swarmService.swarmLogin(loginCredentials, testActor), 5 second)
      assert(result == HttpResponse.apply().addHeader(auth).headers)
    }
  }

}
