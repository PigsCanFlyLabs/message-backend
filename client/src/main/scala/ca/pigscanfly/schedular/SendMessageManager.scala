package ca.pigscanfly.schedular

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import ca.pigscanfly.models.ScheduleSendMessageRequest
import ca.pigscanfly.schedular.SendMessagesScheduler.StoreMessagesInState
import ca.pigscanfly.service.SwarmService
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "type")
trait SerializableMessage

case class CollectMessages(message: ScheduleSendMessageRequest) extends SerializableMessage

class SendMessageManager(swarmService: SwarmService, sendMessageActor: ActorRef, getMessageActor: ActorRef)
  extends Actor
    with ActorLogging {

  private var customerManagerMap: Map[String, ActorRef] = Map.empty[String, ActorRef]

  override def receive: Receive = {
    case CollectMessages(message: ScheduleSendMessageRequest) =>
      log.info(s"Sending message to for actor state $message")
      sendMessageActor(message.customerId) forward StoreMessagesInState(message)
  }

  private def sendMessageActor(customerId: String): ActorRef = {
    customerManagerMap.get(customerId) match {
      case Some(accountActor) =>
        accountActor
      case None =>
        customerManagerMap += (customerId -> context.actorOf(
          Props(
            new SendMessagesScheduler(customerId, swarmService, sendMessageActor, getMessageActor)
          ),
          customerId))
        customerManagerMap(customerId)
    }
  }

}
