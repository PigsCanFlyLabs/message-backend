package ca.pigscanfly.actors

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.{HttpHeader, HttpResponse}
import akka.pattern.ask
import akka.testkit.TestKit
import akka.util.Timeout
import ca.pigscanfly.SwarmMessageClient
import ca.pigscanfly.actors.GetMessageActor._
import ca.pigscanfly.dao.UserDAO
import ca.pigscanfly.models.{LoginCredentials, MessageDelivery, MessageRetrieval}
import org.mockito.MockitoSugar
import org.scalatest.Matchers._
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import org.scalatest.concurrent.ScalaFutures
import slick.jdbc.H2Profile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class GetMessageActorTest(_system: ActorSystem) extends TestKit(_system)
  with WordSpecLike with ScalaFutures with BeforeAndAfterAll with MockitoSugar {

  implicit val timeOut: Timeout = Timeout(40 seconds)
  val driver = H2Profile
  val futureAwaitTime: FiniteDuration = 1.minute

  import driver.api.Database

  implicit val db: driver.api.Database = mock[Database]
  val userDAO: UserDAO = mock[UserDAO]
  implicit val futureAwaitDuration: FiniteDuration =
    FiniteDuration(futureAwaitTime.length, futureAwaitTime.unit)
  val swarmMessageClient: SwarmMessageClient = mock[SwarmMessageClient]

  def this() = this(ActorSystem("AccountActorSystem"))

  "GetMessageActorTest" must {
    "be able to get Email Or Phone From Device Id" in {
      val actorRef = system.actorOf(Props(new GetMessageActor(userDAO, swarmMessageClient) {
        when(userDAO.getEmailOrPhoneFromDeviceId(0L)) thenReturn Future(Some(Some("9876543210"), Some("email@domain.com"), Some("customer-id")))
      }))
      val result = (actorRef ? GetEmailOrPhoneFromDeviceId(0L)).mapTo[Response]
      Thread.sleep(5000)
      whenReady(result) {
        response =>
          response shouldBe GetPhoneOrEmailSuccess(Some("9876543210"), Some("email@domain.com"), Some("customer-id"))
      }
    }

    "not be able to get Email Or Phone From Device Id" in {
      val actorRef = system.actorOf(Props(new GetMessageActor(userDAO, swarmMessageClient) {
        when(userDAO.getEmailOrPhoneFromDeviceId(0L)) thenReturn Future(None)
      }))
      val result = (actorRef ? GetEmailOrPhoneFromDeviceId(0L)).mapTo[Response]
      Thread.sleep(5000)
      whenReady(result) {
        response =>
          response shouldBe GetPhoneOrEmailSuccess(None, None, None)
      }
    }

    "be able to GetMessage" in {
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
      val actorRef = system.actorOf(Props(new GetMessageActor(userDAO, swarmMessageClient) {
        when(swarmMessageClient.getMessages("",
          HttpResponse.apply().headers.toList)) thenReturn Future.successful(MessageRetrieval(List(getMessageResponse)))
      }))
      val result = (actorRef ? GetMessage("", HttpResponse.apply().headers.toList)).mapTo[MessageRetrieval]
      Thread.sleep(5000)
      whenReady(result) {
        response =>
          response.toString shouldBe MessageRetrieval(List(getMessageResponse)).toString
      }
    }

    "be able to SwarmLogin" in {
      val loginCredentials: LoginCredentials = LoginCredentials("username", "password")
      val actorRef = system.actorOf(Props(new GetMessageActor(userDAO, swarmMessageClient) {
        when(swarmMessageClient.login("", loginCredentials)) thenReturn Future.successful(HttpResponse.apply().headers)
      }))
      val result = (actorRef ? SwarmLogin("", loginCredentials)).mapTo[Seq[HttpHeader]]
      Thread.sleep(5000)
      whenReady(result) {
        response =>
          response shouldBe List()
      }
    }

    "be able to MessageAck" in {
      val actorRef = system.actorOf(Props(new GetMessageActor(userDAO, swarmMessageClient) {
        when(swarmMessageClient.ackMessage("", 0, HttpResponse.apply().headers.toList)) thenReturn Future.successful(MessageDelivery(0, "pending"))
      }))
      val result = (actorRef ? MessageAck("", 0, HttpResponse.apply().headers.toList)).mapTo[MessageDelivery]
      Thread.sleep(5000)
      whenReady(result) {
        response =>
          response.toString shouldBe MessageDelivery(0, "pending").toString
      }
    }
  }

}
