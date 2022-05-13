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

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

trait AdminHandler extends JWTTokenHelper with LazyLogging {

  implicit val system: ActorSystem

  implicit val materializer: ActorMaterializer
  implicit val timeOut: Timeout = Timeout(40 seconds)

  /**
   * This method creates new user
   * @param command
   * @param user
   * @return Future[HttpResponse]
   */
  def createUser(command: ActorRef,
                 user: User): Future[HttpResponse] = {
    ask(command, ValidateUserCommand(user.email, user.deviceId)).flatMap {
      case ValidationResponse(false) =>
        ask(command, CreateUserCommand(user)).map {
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
   * Update user details
   * @param command
   * @param user
   * @return Future[HttpResponse]
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
   * Retrieves user details
   * @param command
   * @param deviceId
   * @return Future[HttpResponse]
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
   * Disable user subscription
   * @param command
   * @param request
   * @return Future[HttpResponse]
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
   * Delete user from system
   * @param command
   * @param request
   * @return Future[HttpResponse]
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
   * Create admin user
   * @param command
   * @param admin
   * @return Future[HttpResponse]
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
   * This method is to logg in admin
   * @param command
   * @param adminLogin
   * @return Future[HttpResponse]
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

