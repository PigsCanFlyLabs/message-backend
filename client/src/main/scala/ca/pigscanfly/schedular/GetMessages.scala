package ca.pigscanfly.schedular

import akka.actor.{Actor, Scheduler}
import akka.http.scaladsl.model.headers.Cookie
import ca.pigscanfly.Application.swarmDAO
import ca.pigscanfly.schedular.GetMessages._
import ca.pigscanfly.service.SwarmService

import scala.concurrent.duration.{FiniteDuration, _}
import scala.util.{Failure, Success}

object GetMessages {
  case class ScheduleGetMessage(initialDelay: FiniteDuration, interval: FiniteDuration)
  case object StartGettingMessage
}

class GetMessages(swarmService: SwarmService) extends Actor {

  override def preStart(): Unit = {
    self ! ScheduleGetMessage(0.minute, 1.day)
  }

  def scheduler: Scheduler = context.system.scheduler

  def receive: Receive = {
    case ScheduleGetMessage(initialDelay, delay) =>
      scheduler.scheduleWithFixedDelay(
        initialDelay = initialDelay,
        delay = delay,
        receiver = self,
        message = StartGettingMessage
      )(context.dispatcher)
    case StartGettingMessage =>

  }
  import scala.concurrent.ExecutionContext.Implicits.global
  def getMessages: Unit ={
    val cookieHeader: Cookie = Cookie("JSESSIONID", "B120DCEBC05C9F6CE3FBCA259356C17E") //TODO should get from Login
    val msg = swarmService.getMessages(List(cookieHeader)) onComplete {
      case Success(value) => value.messageResponse.map{message=>
        swarmDAO.getUserDetails(message.deviceId)
      }
      case Failure(exception) =>

    }
  }
}
