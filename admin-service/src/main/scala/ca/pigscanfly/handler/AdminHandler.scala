package ca.pigscanfly.handler

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import ca.pigscanfly.actor.AdminActor._
import ca.pigscanfly.components._
import ca.pigscanfly.models.JWTTokenHelper
import com.typesafe.scalalogging.LazyLogging

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

trait AdminHandler extends JWTTokenHelper with LazyLogging {

  implicit val system: ActorSystem

  implicit val materializer: ActorMaterializer
  implicit val timeOut: Timeout = Timeout(40 seconds)

  /**
   * This method checks if user exists or not in system; if user doesn't exists in the DB it will create new user with the requested (email, phone_number, device_id, is_disabled)
   *
   * @param command : reference of the actor
   * @param user    : contains user's email, phone_number, device_id and is_disabled(identifier to check if user has subscription or not)
   * @return Future[HttpResponse]:
   *         OK(200): If user is created successfully
   *         Conflict(409): If user already exists OR if failed to create new user
   */
  def createUser(command: ActorRef,
                 user: User): Future[HttpResponse] = {
    ask(command, ValidateUserCommand(user.email, user.deviceId)).flatMap {
      case ValidationResponse(false) =>
        ask(command, CreateUserCommand(user.copy(customerId = Some(UUID.randomUUID().toString)))).map {
          case Updated(true) =>
            HttpResponse(status = StatusCodes.OK,
              headers = Nil,
              entity = HttpEntity.Empty,
              protocol = HttpProtocols.`HTTP/1.1`)
          case Updated(false) =>
            HttpResponse(status = StatusCodes.Conflict,
              headers = Nil,
              entity = HttpEntity.Empty,
              protocol = HttpProtocols.`HTTP/1.1`)
        }
      case ValidationResponse(true) =>
        Future.successful(HttpResponse(status = StatusCodes.Conflict,
          headers = Nil,
          entity = HttpEntity.Empty,
          protocol = HttpProtocols.`HTTP/1.1`))
    }
  }

  //update user name

  /**
   * This method updates user's email and phone_number on the basis of device_id
   *
   * @param command : reference of the actor
   * @param user
   * @return Future[HttpResponse]:
   *         OK(200): If succeed to update user details
   *         Conflict(409): If user doesn't exist OR if failed to update user details
   */
  def updateUser(command: ActorRef,
                 user: UpdateUserRequest): Future[HttpResponse] = {
    ask(command, ValidateUserCommand(user.email, user.deviceId)).flatMap {
      case ValidationResponse(true) =>
        ask(command, UpdateUserCommand(user)).map {
          case Updated(true) =>
            HttpResponse(status = StatusCodes.OK,
              headers = Nil,
              entity = HttpEntity.Empty,
              protocol = HttpProtocols.`HTTP/1.1`)
          case Updated(false) =>
            HttpResponse(status = StatusCodes.Conflict,
              headers = Nil,
              entity = HttpEntity.Empty,
              protocol = HttpProtocols.`HTTP/1.1`)
        }
      case ValidationResponse(false) =>
        Future.successful(HttpResponse(status = StatusCodes.Conflict,
          headers = Nil,
          entity = HttpEntity.Empty,
          protocol = HttpProtocols.`HTTP/1.1`))
    }
  }

  /**
   * This method gets user details on the basis of user's device_id
   *
   * @param command  : reference of the actor
   * @param deviceId : user's device_id
   * @return Future[HttpResponse]:
   *         OK(200): If user details are found
   *         NoContent(204): If user details are not found
   *
   */
  def getUserDetails(command: ActorRef,
                     deviceId: Long): Future[HttpResponse] = {
    ask(command, GetUserCommand(deviceId)).map {
      case _: NoDataFound => HttpResponse(status = StatusCodes.NoContent,
        headers = Nil,
        entity = HttpEntity.Empty,
        protocol = HttpProtocols.`HTTP/1.1`)
      case response: GetUserDetailsResponse => HttpResponse(status = StatusCodes.OK,
        headers = Nil,
        entity = HttpEntity(
          ContentTypes.`application/json`,
          response.details.toString),
        protocol = HttpProtocols.`HTTP/1.1`)
    }
  }

  /**
   * This method disables user's subscription on the basis of device_id
   *
   * @param command : reference of the actor
   * @param request : contains device_id
   * @return Future[HttpResponse]:
   *         OK(200): If succeed to disable user
   *         Conflict(409) If failed to disable user
   */
  def disableUser(command: ActorRef,
                  request: DisableUserRequest): Future[HttpResponse] = {
    ask(command, DisableUserCommand(request)).map {
      case Updated(false) => HttpResponse(status = StatusCodes.Conflict,
        headers = Nil,
        entity = HttpEntity.Empty,
        protocol = HttpProtocols.`HTTP/1.1`)
      case Updated(true) => HttpResponse(status = StatusCodes.OK,
        headers = Nil,
        entity = HttpEntity.Empty,
        protocol = HttpProtocols.`HTTP/1.1`)
    }
  }

  /**
   * This method deletes a user from system on the basis of device_id
   *
   * @param command : reference of the actor
   * @param request : contains user's device_id
   * @return Future[HttpResponse]:
   *         OK(200): If succeed to delete user
   *         Conflict(409): If failed to delete user
   */
  def deleteUser(command: ActorRef,
                 request: DeleteUserRequest): Future[HttpResponse] = {
    ask(command, DeleteUserCommand(request)).map {
      case Updated(false) => HttpResponse(status = StatusCodes.Conflict,
        headers = Nil,
        entity = HttpEntity.Empty,
        protocol = HttpProtocols.`HTTP/1.1`)
      case Updated(true) => HttpResponse(status = StatusCodes.OK,
        headers = Nil,
        entity = HttpEntity.Empty,
        protocol = HttpProtocols.`HTTP/1.1`)
    }
  }

  /**
   * This method creates admin or super admin user with the requested email, password and role
   *
   * @param command : reference of the actor
   * @param admin   : request contains email, password and role
   * @return Future[HttpResponse]:
   *         OK(200): If succeed to create new admin or super admin
   *         Conflict(409): If admin/super admin already exists OR failed to create admin/super admin
   */
  def createAdmin(command: ActorRef,
                  admin: AdminLogin): Future[HttpResponse] = {
    ask(command, ValidateAdminCommand(admin.email, admin.role)).flatMap {
      case ValidationResponse(false) =>
        ask(command, CreateAdminCommand(admin)).map {
          case Updated(true) =>
            HttpResponse(status = StatusCodes.OK,
              headers = Nil,
              entity = HttpEntity.Empty,
              protocol = HttpProtocols.`HTTP/1.1`)
          case Updated(false) =>
            HttpResponse(status = StatusCodes.Conflict,
              headers = Nil,
              entity = HttpEntity.Empty,
              protocol = HttpProtocols.`HTTP/1.1`)
        }
      case ValidationResponse(true) =>
        Future.successful(HttpResponse(status = StatusCodes.Conflict,
          headers = Nil,
          entity = HttpEntity.Empty,
          protocol = HttpProtocols.`HTTP/1.1`))
    }
  }

  /**
   * This method is responsible for admin or super admin login
   *
   * @param command    : reference of the actor
   * @param adminLogin : request that contains email, password and role
   * @return Future[HttpResponse]:
   *         OK(200): If admin OR super admin is validated for login
   *         Conflict(409): If admin OR super admin is not valid for login
   */
  def adminLogin(command: ActorRef,
                 adminLogin: AdminLogin): Future[HttpResponse] = {
    ask(command, AdminLoginCommand(adminLogin)).map {
      case ValidationResponse(false) =>
        HttpResponse(status = StatusCodes.Conflict,
          headers = Nil,
          entity = HttpEntity.Empty,
          protocol = HttpProtocols.`HTTP/1.1`)
      case ValidationResponse(true) =>
        HttpResponse(status = StatusCodes.OK,
          headers = Nil,
          entity = HttpEntity(
            ContentTypes.`application/json`,
            createJwtTokenWithRole(adminLogin.email, adminLogin.role)),
          protocol = HttpProtocols.`HTTP/1.1`)
    }

  }


}

