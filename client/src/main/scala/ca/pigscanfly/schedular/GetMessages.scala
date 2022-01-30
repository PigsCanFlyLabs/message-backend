package ca.pigscanfly.schedular

import akka.actor.{Actor, Scheduler}
import akka.http.scaladsl.model.HttpHeader
import ca.pigscanfly.configs.ClientConstants.{swarmPassword, swarmUserName}
import ca.pigscanfly.models.LoginCredentials
import ca.pigscanfly.proto.MessageDataPB.MessageDataPB
import ca.pigscanfly.schedular.GetMessages._
import ca.pigscanfly.sendgrid.SendGridEmailer
import ca.pigscanfly.service.{SwarmService, TwilioService}
import ca.pigscanfly.util.ProtoUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{FiniteDuration, _}
import scala.util.{Failure, Success}

object GetMessages {

  case class ScheduleGetMessage(initialDelay: FiniteDuration, interval: FiniteDuration)

  case object StartGettingMessage

}

class GetMessages(swarmService: SwarmService, twilioService: TwilioService) extends Actor with SendGridEmailer with ProtoUtils {

  val loginCookie = swarmService.swarmLogin(LoginCredentials(swarmUserName, swarmPassword))

  override def preStart(): Unit = {
    // TODO TIME SHOULD BE DRIVEN FROM CONFIG
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
          swarmService.getPhoneOrEmailFromDeviceId(message.deviceId) onComplete {
            case Success(fromInfo) =>
              fromInfo.phone.fold(throw new Exception(s"Didn't found sender phone details of Device ID ::: ${message.deviceId}")) { fromPhone =>
                val messageDataPB: MessageDataPB = decodeGetMessage(message.data)
                messageDataPB.message.map { messageData =>
                  twilioService.sendToTwilio(messageData.fromOrTo, fromPhone, messageData.text)
                }
              }
            //                sendMail(usr.email, message.data)
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
