package ca.pigscanfly.schedular

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Scheduler}
import ca.pigscanfly.components.MessageHistory
import ca.pigscanfly.configs.ClientConstants.{messagePollingDelay, schedulerInitialDelay, schedulerInterval}
import ca.pigscanfly.models.{GetMessage, ScheduleSendMessageRequest}
import ca.pigscanfly.proto.MessageDataPB.MessageDataPB
import ca.pigscanfly.schedular.SendMessagesScheduler._
import ca.pigscanfly.sendgrid.SendGridEmailer
import ca.pigscanfly.service.{SwarmService, TwilioService}
import ca.pigscanfly.util.Constants.{EMAIl, SMS}
import ca.pigscanfly.util.{ProtoUtils, Validations}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object SendMessagesScheduler {

  def props(customerId: String, swarmService: SwarmService, sendMessageActor: ActorRef, getMessageActor: ActorRef): Props = {
    Props(new SendMessagesScheduler(customerId, swarmService, sendMessageActor, getMessageActor))
  }

  case class ScheduleSendMessage(pollingDelay: FiniteDuration)

  case class StoreMessagesInState(message: ScheduleSendMessageRequest)

  case object StartSendingMessage

}

class SendMessagesScheduler(customerId: String, swarmService: SwarmService, sendMessageActor: ActorRef, getMessageActor: ActorRef) extends Actor with SendGridEmailer
  with Validations with ProtoUtils with ActorLogging {

  var state: Seq[ScheduleSendMessageRequest] = Seq()

  override def preStart(): Unit = {
    log.info(s"Actor started to schedule send messages current state: $state")
    self ! ScheduleSendMessage(messagePollingDelay.minute)

  }

  def receive: Receive = {
    case ScheduleSendMessage(pollingDelay) =>
      scheduler.scheduleOnce(delay = pollingDelay,
        receiver = self,
        message = StartSendingMessage
      )(context.dispatcher)
    case StartSendingMessage =>
      log.info(s"Sending collected messages to messages ${state}")
      sendMessages
    case StoreMessagesInState(message: ScheduleSendMessageRequest) =>
      log.info(s"Collecting message: $message in actor state")
      collectMessages(message)
  }

  def collectMessages(message: ScheduleSendMessageRequest): Unit = {
    state = state :+ message
  }

  def scheduler: Scheduler = context.system.scheduler

  def sendMessages: Unit = {
    swarmService.sendMessage(sendMessageActor, getMessageActor, state) onComplete {
      case Success(messageRetrieval) =>
        state = Seq()
        log.info(s"Succeed to send messages: $messageRetrieval for customer: $customerId")
      case Failure(exception) =>
        throw new Exception(s"Got exception while hitting get messages route, ex ::: ${exception.getMessage}")
    }

  }
}
