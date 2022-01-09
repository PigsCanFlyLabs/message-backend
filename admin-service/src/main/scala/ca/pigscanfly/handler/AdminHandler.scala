package ca.pigscanfly.handler

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import ca.pigscanfly.actor.AdminActor._
import ca.pigscanfly.components._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

trait AdminHandler {

  implicit val system: ActorSystem

  implicit val materializer: ActorMaterializer
  implicit val timeOut: Timeout = Timeout(40 seconds)

  def getAppHealth(command: ActorRef): Future[HttpResponse] = {
    ask(command, TestActorCall).mapTo[Int].map {
      case 0 =>
        HttpResponse.apply()
      case 1 =>
        HttpResponse.apply()
    }
  }

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
  def updateUser(command: ActorRef,
                 user: User): Future[HttpResponse] = {
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

  def getUserDetails(command: ActorRef,
                     email: String,
                     deviceId: String): Future[HttpResponse] = {
    ask(command, GetUserCommand(email, deviceId)).map {
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
          entity = HttpEntity.Empty,
          protocol = HttpProtocols.`HTTP/1.1`)
    }

  }


}

