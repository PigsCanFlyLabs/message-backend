package ca.pigscanfly.schedular

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Scheduler}
import ca.pigscanfly.components.MessageHistory
import ca.pigscanfly.configs.ClientConstants.{schedulerInitialDelay, schedulerInterval}
import akka.actor.{Actor, ActorLogging, ActorRef, Props, Scheduler}
import ca.pigscanfly.configs.ClientConstants.{schedulerInitialDelay, schedulerInterval}
import ca.pigscanfly.models.GetMessage
import ca.pigscanfly.proto.MessageDataPB.MessageDataPB
import ca.pigscanfly.schedular.GetMessagesScheduler._
import ca.pigscanfly.sendgrid.SendGridEmailer
import ca.pigscanfly.service.{SwarmService, TwilioService}
import ca.pigscanfly.util.Constants.{EMAIl, SMS}
import ca.pigscanfly.util.{ProtoUtils, Validations}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{FiniteDuration, _}
import scala.util.{Failure, Success}

object GetMessagesScheduler {

  def props(swarmService: SwarmService, twilioService: TwilioService, getMessageActor: ActorRef): Props = {
    Props(new GetMessagesScheduler(swarmService, twilioService, getMessageActor))
  }

  case class ScheduleGetMessage(initialDelay: FiniteDuration, interval: FiniteDuration)

  case object StartGettingMessage

}

class GetMessagesScheduler(swarmService: SwarmService, twilioService: TwilioService, getMessageActor: ActorRef) extends Actor with SendGridEmailer
  with Validations with ProtoUtils with ActorLogging {

  /**
   * When the application will start Actor scheduler will start fetching messages from the Swarm Satellite with the delay of 2 minutes
   */
  override def preStart(): Unit = {
    self ! ScheduleGetMessage(schedulerInitialDelay.minute, schedulerInterval.minutes)

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

  /**
   * This method is responsible for retrieving messages from Swarm Satellite. It perform listed operations:
   * Fetch messages from Swarm Satellite
   * Get Email or Phone from the device_id of retrieved message
   * Decode retrieved message to find message to be send and receiver of the message
   * Detect source destination from the decoded receiver information and send email or SMS accordingly
   */
  def getMessages: Unit = {
    swarmService.getMessages(getMessageActor) onComplete {
      case Success(messageRetrieval) => messageRetrieval.messageResponse.map { message: GetMessage =>
        swarmService.getPhoneOrEmailFromDeviceId(message.deviceId, getMessageActor) onComplete {
          case Success(fromInfo) =>
            val messageDataPB: MessageDataPB = decodeGetMessage(message.data)
            messageDataPB.message.map { messageData =>
              log.info(s"GetMessagesScheduler: Detecting source destination: ${messageData.to}")
              val sourceDestination = detectSourceDestination(messageData.to)
              sourceDestination match {
                case EMAIl =>
                  log.info(s"GetMessagesScheduler: Detected source destination as EMAIL for TO: ${messageData.to}")
                  val messageHistory = MessageHistory(message.deviceId,
                    messageData.to,
                    sourceDestination,
                    "GET", message.packetId)
                  swarmService.saveMessageHistory(messageHistory, getMessageActor)
                  sendMail(messageData.to, message.data)
                case SMS =>
                  log.info(s"GetMessagesScheduler: Detected source destination as SMS for TO: ${messageData.to}")
                  fromInfo.phone.fold(throw new Exception(s"Didn't found sender phone details of Device ID ::: ${message.deviceId}")) { fromPhone =>
                    val messageHistory = MessageHistory(message.deviceId,
                      messageData.to,
                      sourceDestination,
                      "GET", message.packetId)
                    swarmService.saveMessageHistory(messageHistory, getMessageActor)
                    sendMail(messageData.to, message.data)
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
