package ca.pigscanfly.schedular

import akka.actor.{Actor, ActorLogging, Props, Scheduler}
import ca.pigscanfly.configs.ClientConstants.{schedulerInitalDelay, schedulerInterval}
import ca.pigscanfly.models.{GetMessage, MessageRetrieval}
import ca.pigscanfly.proto.MessageDataPB.{Message, MessageDataPB, Protocol}
import ca.pigscanfly.schedular.GetMessagesScheduler._
import ca.pigscanfly.sendgrid.SendGridEmailer
import ca.pigscanfly.service.{SwarmService, TwilioService}
import ca.pigscanfly.util.Constants.{EMAIl, SMS}
import ca.pigscanfly.util.{ProtoUtils, Validations}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.{FiniteDuration, _}
import scala.util.{Failure, Success}

object GetMessagesScheduler {

  case class ScheduleGetMessage(initialDelay: FiniteDuration, interval: FiniteDuration)

  case object StartGettingMessage

  def props(swarmService: SwarmService, twilioService: TwilioService): Props ={
    Props(new GetMessagesScheduler(swarmService, twilioService))
  }

}

class GetMessagesScheduler(swarmService: SwarmService, twilioService: TwilioService) extends Actor with SendGridEmailer
  with Validations with ProtoUtils  with ActorLogging{

  override def preStart(): Unit = {
    self ! ScheduleGetMessage(schedulerInitalDelay.minute, schedulerInterval.minutes)

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
      log.info("GetMessagesScheduler: Fetching messages from Swarm")
      getMessages
  }

  def scheduler: Scheduler = context.system.scheduler

  def getMessages: Unit = {
    swarmService.getMessages() onComplete {
      case Success(messageRetrieval) => messageRetrieval.messageResponse.map { message: GetMessage =>
        swarmService.getPhoneOrEmailFromDeviceId(message.deviceId) onComplete {
          case Success(fromInfo) =>
            val messageDataPB: MessageDataPB = decodeGetMessage(message.data)
            messageDataPB.message.map { messageData =>
              log.info(s"GetMessagesScheduler: Detecting source destination: ${messageData.to}")
              detectSourceDestination(messageData.to) match {
                case EMAIl => sendMail(messageData.to, message.data)
                case SMS =>
                  fromInfo.phone.fold(throw new Exception(s"Didn't found sender phone details of Device ID ::: ${message.deviceId}")) { fromPhone =>
                    twilioService.sendToTwilio(messageData.to, fromPhone, messageData.text)
                  }
                case _ =>
                  throw new Exception(s"Can't send message to UnKnown destination for device Id ::: ${message.deviceId}")
              }
            }
          case Failure(exception) =>
            throw new Exception(s"Got exception while hitting get user details route for device ID ::: ${message.deviceId}, ex ::: ${exception.getMessage}")
        }
      }
      case Failure(exception) =>
        throw new Exception(s"Got exception while hitting get messages route, ex ::: ${exception.getMessage}")
    }

  }
}
