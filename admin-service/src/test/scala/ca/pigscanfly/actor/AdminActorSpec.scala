package ca.pigscanfly.actor

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import ca.pigscanfly.actor.AdminActor._
import ca.pigscanfly.components.{AdminLogin, DeleteUserRequest, DisableUserRequest, User}
import ca.pigscanfly.dao.{AdminDAO, UserDAO}
import org.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpecLike}
import slick.jdbc.H2Profile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class AdminActorSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with MustMatchers with BeforeAndAfterAll with MockitoSugar {
  val driver = H2Profile
  val futureAwaitTime: FiniteDuration = 10.minute

  import driver.api.Database

  implicit val db: driver.api.Database = mock[Database]
  implicit val schema: String = ""
  val adminDAO = mock[AdminDAO]
  val userDAO = mock[UserDAO]
  val user = User(deviceId = 1L, phone = Some("9876543210"), email = Some("email@domain.com"), isDisabled = false)


  implicit val futureAwaitDuration: FiniteDuration =
    FiniteDuration(futureAwaitTime.length, futureAwaitTime.unit)
  val disableUserRequest = DisableUserRequest(deviceId = user.deviceId, email = user.email.getOrElse(""), isDisabled = true)
  val deleteUserRequest = DeleteUserRequest(deviceId = user.deviceId, email = user.email.getOrElse(""))
  val adminLoginRequest: AdminLogin = AdminLogin("email", "password", "role")

  def this() = this(ActorSystem("AdminActorSystem"))

  "A AdminActor" must {

    "be able to check If User Exists" in {
      val actorRef = system.actorOf(Props(new AdminActor(adminDAO, userDAO) {
        when(userDAO.checkIfUserExists(Some("email"), 0L)) thenReturn Future(1)
      }))
      actorRef ! ValidateUserCommand(Some("email"), 0L)
      expectMsgType[ValidationResponse](5 seconds)
    }

    "not be able to check If User Exists" in {
      val actorRef = system.actorOf(Props(new AdminActor(adminDAO, userDAO) {
        when(userDAO.checkIfUserExists(Some("email"), 0L)) thenReturn Future(0)
      }))
      actorRef ! ValidateUserCommand(Some("email"), 0L)
      expectMsgType[ValidationResponse](5 seconds)
    }

    "be able to get User Details" in {
      val actorRef = system.actorOf(Props(new AdminActor(adminDAO, userDAO) {
        when(userDAO.getUserDetails(0L)) thenReturn Future(None)
      }))
      actorRef ! GetUserCommand(0L)
      expectMsgType[NoDataFound](5 seconds)
    }

    "not be able to get User Details" in {
      val actorRef = system.actorOf(Props(new AdminActor(adminDAO, userDAO) {
        when(userDAO.getUserDetails(1L)) thenReturn Future(Some(user))
      }))
      actorRef ! GetUserCommand(1L)
      expectMsgType[GetUserDetailsResponse](5 seconds)
    }

    "be able to create User Details" in {
      val actorRef = system.actorOf(Props(new AdminActor(adminDAO, userDAO) {
        when(userDAO.insertUserDetails(user)) thenReturn Future(1)
      }))
      actorRef ! CreateUserCommand(user)
      expectMsgType[Updated](5 seconds)
    }

    "not be able to create User Details" in {
      val actorRef = system.actorOf(Props(new AdminActor(adminDAO, userDAO) {
        when(userDAO.insertUserDetails(user)) thenReturn Future(0)
      }))
      actorRef ! CreateUserCommand(user)
      expectMsgType[Updated](5 seconds)
    }

    "be able to update User Details" in {
      val actorRef = system.actorOf(Props(new AdminActor(adminDAO, userDAO) {
        when(userDAO.updateUserDetails(user)) thenReturn Future(1)
      }))
      actorRef ! UpdateUserCommand(user)
      expectMsgType[Updated](5 seconds)
    }

    "not be able to update User Details" in {
      val actorRef = system.actorOf(Props(new AdminActor(adminDAO, userDAO) {
        when(userDAO.updateUserDetails(user)) thenReturn Future(0)
      }))
      actorRef ! UpdateUserCommand(user)
      expectMsgType[Updated](5 seconds)
    }

    "be able to disable User" in {
      val actorRef = system.actorOf(Props(new AdminActor(adminDAO, userDAO) {
        when(userDAO.disableUser(disableUserRequest)) thenReturn Future(1)
      }))
      actorRef ! DisableUserCommand(disableUserRequest)
      expectMsgType[Updated](5 seconds)
    }

    "not be able to disable User" in {
      val actorRef = system.actorOf(Props(new AdminActor(adminDAO, userDAO) {
        when(userDAO.disableUser(disableUserRequest)) thenReturn Future(0)
      }))
      actorRef ! DisableUserCommand(disableUserRequest)
      expectMsgType[Updated](5 seconds)
    }

    "be able to delete User" in {
      val actorRef = system.actorOf(Props(new AdminActor(adminDAO, userDAO) {
        when(userDAO.deleteUser(deleteUserRequest)) thenReturn Future(1)
      }))
      actorRef ! DeleteUserCommand(deleteUserRequest)
      expectMsgType[Updated](5 seconds)
    }

    "not be able to delete User" in {
      val actorRef = system.actorOf(Props(new AdminActor(adminDAO, userDAO) {
        when(userDAO.deleteUser(deleteUserRequest)) thenReturn Future(0)
      }))
      actorRef ! DeleteUserCommand(deleteUserRequest)
      expectMsgType[Updated](5 seconds)
    }

    "not be able to validate admin" in {
      val actorRef = system.actorOf(Props(new AdminActor(adminDAO, userDAO) {
        when(adminDAO.checkIfAdminExists("email", "admin")) thenReturn Future(0)
      }))
      actorRef ! ValidateAdminCommand("email", "admin")
      expectMsgType[ValidationResponse](5 seconds)
    }

    "be able to validate admin" in {
      val actorRef = system.actorOf(Props(new AdminActor(adminDAO, userDAO) {
        when(adminDAO.checkIfAdminExists("email", "admin")) thenReturn Future(1)
      }))
      actorRef ! ValidateAdminCommand("email", "admin")
      expectMsgType[ValidationResponse](5 seconds)
    }

    "be able to validate admin login" in {
      val actorRef = system.actorOf(Props(new AdminActor(adminDAO, userDAO) {
        when(adminDAO.validateAdminLogin(adminLoginRequest)) thenReturn Future(1)
      }))
      actorRef ! AdminLoginCommand(adminLoginRequest)
      expectMsgType[ValidationResponse](5 seconds)
    }

    "not be able to validate admin login" in {
      val actorRef = system.actorOf(Props(new AdminActor(adminDAO, userDAO) {
        when(adminDAO.validateAdminLogin(adminLoginRequest)) thenReturn Future(0)
      }))
      actorRef ! AdminLoginCommand(adminLoginRequest)
      expectMsgType[ValidationResponse](5 seconds)
    }

    "be able to create admin login" in {
      val actorRef = system.actorOf(Props(new AdminActor(adminDAO, userDAO) {
        when(adminDAO.createAdminUser(adminLoginRequest)) thenReturn Future(1)
      }))
      actorRef ! CreateAdminCommand(adminLoginRequest)
      expectMsgType[Updated](5 seconds)
    }

    "not be able to create admin login" in {
      val actorRef = system.actorOf(Props(new AdminActor(adminDAO, userDAO) {
        when(adminDAO.createAdminUser(adminLoginRequest)) thenReturn Future(0)
      }))
      actorRef ! CreateAdminCommand(adminLoginRequest)
      expectMsgType[Updated](5 seconds)
    }

  }
}
