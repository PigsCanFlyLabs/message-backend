package ca.pigscanfly.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import ca.pigscanfly.actors.GetMessageActor._
import ca.pigscanfly.dao.{AdminDAO, UserDAO}
import org.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpecLike}
import slick.jdbc.H2Profile
//import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class GetMessageActorTest extends TestKit(ActorSystem("Test")) with ImplicitSender
  with WordSpecLike with MustMatchers with BeforeAndAfterAll with MockitoSugar {
  val driver = H2Profile
  val futureAwaitTime: FiniteDuration = 10.minute

  import driver.api.Database

  implicit val db: driver.api.Database = mock[Database]
  implicit val schema: String = ""
  val adminDAO: AdminDAO = mock[AdminDAO]
  val userDAO = mock[UserDAO]


  implicit val futureAwaitDuration: FiniteDuration =
    FiniteDuration(futureAwaitTime.length, futureAwaitTime.unit)

  "A GetMessageActor" must {
    val actorRef = system.actorOf(Props(new GetMessageActor(userDAO)))

    "be able to get Email Or Phone From Device Id" in {
      when(userDAO.getEmailOrPhoneFromDeviceId(0L)) thenReturn Future.successful(Some(Some(""), Some("")))
      actorRef ! GetEmailOrPhoneFromDeviceId(0L)
      expectMsgType[Response](15 seconds)
    }

    "not be able to get Email Or Phone From Device Id" in {
      val actorRef = system.actorOf(Props(new GetMessageActor(userDAO) {
        when(userDAO.getEmailOrPhoneFromDeviceId(0L)) thenReturn Future(None)
      }))
      actorRef ! GetEmailOrPhoneFromDeviceId(0L)
      expectMsgType[GetPhoneOrEmailSuccess](5 seconds)
    }
  }

}
