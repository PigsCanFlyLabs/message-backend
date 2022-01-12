package ca.pigscanfly.schedular

import akka.actor.{Actor, Scheduler}
import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers.Cookie
import ca.pigscanfly.Application.swarmDAO
import ca.pigscanfly.configs.ClientConstants.{swarmPassword, swarmUserName}
import ca.pigscanfly.models.LoginCredentials
import ca.pigscanfly.schedular.GetMessages._
import ca.pigscanfly.sendgrid.SendGridEmailer
import ca.pigscanfly.service.SwarmService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{FiniteDuration, _}
import scala.util.{Failure, Success}

object GetMessages {

  case class ScheduleGetMessage(initialDelay: FiniteDuration, interval: FiniteDuration)

  case object StartGettingMessage

}

class GetMessages(swarmService: SwarmService) extends Actor with SendGridEmailer {

  val loginCookie = swarmService.swarmLogin(LoginCredentials(swarmUserName, swarmPassword))

  override def preStart(): Unit = {
    self ! ScheduleGetMessage(10.minute, 3.minutes)

  }

  def receive: Receive = {
    case ScheduleGetMessage(initialDelay, delay) =>
      scheduler.scheduleWithFixedDelay(
        initialDelay = initialDelay,
        delay = delay,
        receiver = self,
        message = StartGettingMessage
      )(context.dispatcher)
    case StartGettingMessage =>
      getMessages
  }

  def scheduler: Scheduler = context.system.scheduler

  def getMessages: Unit = {
    loginCookie.map { cookie: Seq[HttpHeader] =>
      swarmService.getMessages(cookie) onComplete {
        case Success(messageRetrieval) => messageRetrieval.messageResponse.map { message =>
          swarmDAO.getUserDetails(message.deviceId) onComplete {
            case Success(user) =>
              user.fold(throw new Exception(s"Didn't found user details of Device ID ::: ${message.deviceId}")) { usr =>
                sendMail(usr.email, message.data)
              }
            case Failure(exception) =>
              throw new Exception(s"Got exception while hitting get user details route, ex ::: ${exception.getMessage}")
          }
        }
        case Failure(exception) =>
          throw new Exception(s"Got exception while hitting get messages route, ex ::: ${exception.getMessage}")
      }
    }

  }
}
