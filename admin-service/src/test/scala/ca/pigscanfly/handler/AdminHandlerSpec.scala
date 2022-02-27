package ca.pigscanfly.handler

import akka.actor.{Actor, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer
import akka.testkit.TestActorRef
import ca.pigscanfly.actor.AdminActor._
import ca.pigscanfly.components._
import ca.pigscanfly.dao.{AdminDAO, UserDAO}
import org.mockito.MockitoSugar
import org.scalatest.WordSpec

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class AdminHandlerSpec extends WordSpec with AdminHandler with MockitoSugar {

  implicit val userDAO: UserDAO = mock[UserDAO]
  implicit val adminDAO: AdminDAO = mock[AdminDAO]

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val user = User(deviceId = 1L, phone = Some("9876543210"), email = Some("email@domain.com"), isDisabled = false)
  val disableUserRequest = DisableUserRequest(deviceId = user.deviceId, email = user.email.getOrElse(""), isDisabled = true)
  val deleteUserRequest = DeleteUserRequest(deviceId = user.deviceId, email = user.email.getOrElse(""))
  val adminLoginRequest: AdminLogin = AdminLogin("email", "password", "role")


  "AdminHandler" should {

    "send Conflict message when trying to create user if user already exists" in {
      val command = TestActorRef(new Actor {
        def receive: Receive = {
          case ValidateUserCommand(_, _) ⇒
            sender ! ValidationResponse(true)
        }
      })
      val result = Await.result(createUser(command, user), 5 second)
      assert(result.status == StatusCodes.Conflict)
    }

    "send Conflict message when failed to create user" in {
      val command = TestActorRef(new Actor {
        def receive: Receive = {
          case ValidateUserCommand(_, _) ⇒
            sender ! ValidationResponse(false)
          case CreateUserCommand(_) =>
            sender ! Updated(false)
        }
      })
      val result = Await.result(createUser(command, user), 5 second)
      assert(result.status == StatusCodes.Conflict)
    }

    "send OK message when succeed to create user" in {
      val command = TestActorRef(new Actor {
        def receive: Receive = {
          case ValidateUserCommand(_, _) ⇒
            sender ! ValidationResponse(false)
          case CreateUserCommand(_) =>
            sender ! Updated(true)
        }
      })
      val result = Await.result(createUser(command, user), 5 second)
      assert(result.status == StatusCodes.OK)
    }


    "send Conflict message when trying to update user if user not exists" in {
      val command = TestActorRef(new Actor {
        def receive: Receive = {
          case ValidateUserCommand(_, _) ⇒
            sender ! ValidationResponse(false)
        }
      })
      val result = Await.result(updateUser(command, user), 5 second)
      assert(result.status == StatusCodes.Conflict)
    }

    "send Conflict message when failed to update user" in {
      val command = TestActorRef(new Actor {
        def receive: Receive = {
          case ValidateUserCommand(_, _) ⇒
            sender ! ValidationResponse(true)
          case UpdateUserCommand(_) =>
            sender ! Updated(false)
        }
      })
      val result = Await.result(updateUser(command, user), 5 second)
      assert(result.status == StatusCodes.Conflict)
    }

    "send OK message when succeed to update user" in {
      val command = TestActorRef(new Actor {
        def receive: Receive = {
          case ValidateUserCommand(_, _) ⇒
            sender ! ValidationResponse(true)
          case UpdateUserCommand(_) =>
            sender ! Updated(true)
        }
      })
      val result = Await.result(updateUser(command, user), 5 second)
      assert(result.status == StatusCodes.OK)
    }

    "send OK message when succeed to get User Details" in {
      val command = TestActorRef(new Actor {
        def receive: Receive = {
          case GetUserCommand(_) ⇒
            sender ! GetUserDetailsResponse(user)
        }
      })
      val result = Await.result(getUserDetails(command, 1L), 5 second)
      assert(result.status == StatusCodes.OK)
    }

    "send NoContent message when failed to get User Details" in {
      val command = TestActorRef(new Actor {
        def receive: Receive = {
          case GetUserCommand(_) ⇒
            sender ! NoDataFound()
        }
      })
      val result = Await.result(getUserDetails(command, 1L), 5 second)
      assert(result.status == StatusCodes.NoContent)
    }

    "send Conflict message when failed to disable User" in {
      val command = TestActorRef(new Actor {
        def receive: Receive = {
          case DisableUserCommand(_) ⇒
            sender ! Updated(false)
        }
      })
      val result = Await.result(disableUser(command, disableUserRequest), 5 second)
      assert(result.status == StatusCodes.Conflict)
    }

    "send OK message when succeed to disable User" in {
      val command = TestActorRef(new Actor {
        def receive: Receive = {
          case DisableUserCommand(_) ⇒
            sender ! Updated(true)
        }
      })
      val result = Await.result(disableUser(command, disableUserRequest), 5 second)
      assert(result.status == StatusCodes.OK)
    }

    "send OK message when succeed to delete User" in {
      val command = TestActorRef(new Actor {
        def receive: Receive = {
          case DeleteUserCommand(_) ⇒
            sender ! Updated(true)
        }
      })
      val result = Await.result(deleteUser(command, deleteUserRequest), 5 second)
      assert(result.status == StatusCodes.OK)
    }

    "send Conflict message when failed to delete User" in {
      val command = TestActorRef(new Actor {
        def receive: Receive = {
          case DeleteUserCommand(_) ⇒
            sender ! Updated(false)
        }
      })
      val result = Await.result(deleteUser(command, deleteUserRequest), 5 second)
      assert(result.status == StatusCodes.Conflict)
    }

    "send Conflict message when failed to create admin as admin already exists" in {
      val command = TestActorRef(new Actor {
        def receive: Receive = {
          case ValidateAdminCommand(_, _) ⇒
            sender ! ValidationResponse(true)
        }
      })
      val result = Await.result(createAdmin(command, adminLoginRequest), 5 second)
      assert(result.status == StatusCodes.Conflict)
    }

    "send Conflict message when failed to create admin" in {
      val command = TestActorRef(new Actor {
        def receive: Receive = {
          case ValidateAdminCommand(_, _) ⇒
            sender ! ValidationResponse(false)
          case CreateAdminCommand(_) =>
            sender ! Updated(false)
        }
      })
      val result = Await.result(createAdmin(command, adminLoginRequest), 5 second)
      assert(result.status == StatusCodes.Conflict)
    }

    "send OK message when succeed to create admin" in {
      val command = TestActorRef(new Actor {
        def receive: Receive = {
          case ValidateAdminCommand(_, _) ⇒
            sender ! ValidationResponse(false)
          case CreateAdminCommand(_) =>
            sender ! Updated(true)
        }
      })
      val result = Await.result(createAdmin(command, adminLoginRequest), 5 second)
      assert(result.status == StatusCodes.OK)
    }

    "send OK message when admin succeed to login" in {
      val command = TestActorRef(new Actor {
        def receive: Receive = {
          case AdminLoginCommand(_) =>
            sender ! ValidationResponse(true)
        }
      })
      val result = Await.result(adminLogin(command, adminLoginRequest), 5 second)
      assert(result.status == StatusCodes.OK)
    }

    "send Conflict message when admin failed to login" in {
      val command = TestActorRef(new Actor {
        def receive: Receive = {
          case AdminLoginCommand(_) =>
            sender ! ValidationResponse(false)
        }
      })
      val result = Await.result(adminLogin(command, adminLoginRequest), 5 second)
      assert(result.status == StatusCodes.Conflict)
    }

  }
}
