package ca.pigscanfly.actor

import akka.actor.{ActorLogging, Props}
import akka.pattern.pipe
import ca.pigscanfly.actor.AdminActor._
import ca.pigscanfly.components._
import ca.pigscanfly.dao.{AdminDAO, UserDAO}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

class AdminActor(swarmDAO: AdminDAO, userDAO: UserDAO)(
  implicit futureAwaitDuration: FiniteDuration)
  extends FailurePropatingActor
    with ActorLogging {

  //noinspection ScalaStyle
  override def receive: Receive = {
    case ValidateUserCommand(email: String, deviceId: Long) =>
      val res = userDAO.checkIfUserExists(email, deviceId).map {
        case 1 =>
          ValidationResponse(true)
        case 0 =>
          ValidationResponse(false)
      }
      res.pipeTo(sender())

    case GetUserCommand(deviceId: Long) =>
      val res = userDAO.getUserDetails(deviceId).map {
        case None =>
          NoDataFound()
        case Some(details) =>
          GetUserDetailsResponse(details)
      }
      res.pipeTo(sender())

    case CreateUserCommand(user: User) =>
      val res = userDAO.insertUserDetails(user).map {
        case 0 =>
          Updated(false)
        case 1 =>
          Updated(true)
      }
      res.pipeTo(sender())

    case UpdateUserCommand(user: User) =>
      val res = userDAO.updateUserDetails(user).map {
        case 0 =>
          Updated(false)
        case 1 =>
          Updated(true)
      }
      res.pipeTo(sender())

    case DisableUserCommand(request: DisableUserRequest) =>
      val res = userDAO.disableUser(request).map {
        case 0 =>
          Updated(false)
        case 1 =>
          Updated(true)
      }
      res.pipeTo(sender())

    case DeleteUserCommand(request: DeleteUserRequest) =>
      val res = userDAO.deleteUser(request).map {
        case 0 =>
          Updated(false)
        case 1 =>
          Updated(true)
      }
      res.pipeTo(sender())

    case ValidateAdminCommand(email: String, role: String) =>
      val res = swarmDAO.checkIfAdminExists(email, role).map {
        case 1 =>
          ValidationResponse(true)
        case 0 =>
          ValidationResponse(false)
      }
      res.pipeTo(sender())

    case AdminLoginCommand(adminLogin: AdminLogin) =>
      val res = swarmDAO.validateAdminLogin(adminLogin).map {
        case 0 =>
          ValidationResponse(false)
        case 1 =>
          ValidationResponse(true)
      }
      res.pipeTo(sender())

    case CreateAdminCommand(admin: AdminLogin) =>
      val res = swarmDAO.createAdminUser(admin).map {
        case 0 =>
          Updated(false)
        case 1 =>
          Updated(true)
      }
      res.pipeTo(sender())
  }
}

object AdminActor {
  def props(swarmDAO: AdminDAO, userDAO: UserDAO)(
    implicit futureAwaitDuration: FiniteDuration): Props =
    Props(
      new AdminActor(swarmDAO, userDAO))

  // commands
  sealed trait Command

  sealed trait Response

  final case class ValidationResponse(status: Boolean) extends Response

  final case class Updated(status: Boolean) extends Response

  final case class NoDataFound() extends Response

  final case class GetUserDetailsResponse(details: User) extends Response

  final case class ValidateUserCommand(email: String, deviceId: Long) extends Command

  final case class GetUserCommand(deviceId: Long) extends Command

  final case class CreateUserCommand(user: User) extends Command

  final case class UpdateUserCommand(user: User) extends Command

  final case class DisableUserCommand(request: DisableUserRequest) extends Command

  final case class DeleteUserCommand(request: DeleteUserRequest) extends Command

  final case class ValidateAdminCommand(email: String, role: String) extends Command

  final case class AdminLoginCommand(adminLogin: AdminLogin) extends Command

  final case class CreateAdminCommand(adminLogin: AdminLogin) extends Command
}
