package ca.pigscanfly.actors

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.{HttpHeader, HttpResponse}
import akka.pattern.ask
import akka.testkit.TestKit
import akka.util.Timeout
import ca.pigscanfly.SwarmMessageClient
import ca.pigscanfly.actors.GetMessageActor._
import ca.pigscanfly.actors.SendMessageActor.{CheckDeviceSubscription, CheckSubscription, GetDeviceId, GetDeviceIdFromEmailOrPhone, PostMessageCommand}
import ca.pigscanfly.dao.UserDAO
import ca.pigscanfly.models.{LoginCredentials, MessageDelivery, MessagePost, MessageRetrieval}
import org.mockito.MockitoSugar
import org.scalatest.Matchers._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import slick.jdbc.H2Profile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class SendMessageActorTest(_system: ActorSystem) extends TestKit(_system)
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
    "be able to CheckSubscription" in {
      val actorRef = system.actorOf(Props(new SendMessageActor(userDAO, swarmMessageClient) {
        when(userDAO.checkUserSubscription(0L)) thenReturn Future(Some(true, Some("customer-id")))
      }))
      val result = (actorRef ? CheckSubscription(0L)).mapTo[CheckDeviceSubscription]
      Thread.sleep(5000)
      whenReady(result) {
        response =>
          response shouldBe CheckDeviceSubscription(Some(true), Some("customer-id"))
      }
    }

    "be able to GetDeviceIdFromEmailOrPhone" in {
      val actorRef = system.actorOf(Props(new SendMessageActor(userDAO, swarmMessageClient) {
        when(userDAO.getDeviceIdFromEmailOrPhone("from")) thenReturn Future(Some(0L, Some("customer-id")))
      }))
      val result = (actorRef ? GetDeviceIdFromEmailOrPhone("from")).mapTo[GetDeviceId]
      Thread.sleep(5000)
      whenReady(result) {
        response =>
          response shouldBe GetDeviceId(Some(0L), Some("customer-id"))
      }
    }

    "be able to PostMessageCommand" in {
      val messagePost = MessagePost(deviceType = 1, deviceId = 0L, userApplicationId = 1, data = "message")
      val actorRef = system.actorOf(Props(new SendMessageActor(userDAO, swarmMessageClient) {
        when(swarmMessageClient.sendMessage("url", messagePost, List())) thenReturn Future.successful(MessageDelivery(0, "pending"))
      }))
      val result = (actorRef ? PostMessageCommand("url", messagePost, List())).mapTo[MessageDelivery]
      Thread.sleep(5000)
      whenReady(result) {
        response =>
          response shouldBe MessageDelivery(0, "pending")
      }
    }
  }

}
