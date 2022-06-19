package ca.pigscanfly.service

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers.{Cookie, HttpCookiePair}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import ca.pigscanfly.actors.GetMessageActor._
import ca.pigscanfly.actors.SendMessageActor._
import ca.pigscanfly.actors.{GetMessageActor, SendMessageActor}
import ca.pigscanfly.components.MessageHistory
import ca.pigscanfly.configs.ClientConstants.{swarmPassword, swarmUserName}
import ca.pigscanfly.configs.Constants.SwarmBaseUrl
import ca.pigscanfly.dao.UserDAO
import ca.pigscanfly.{SwarmMessageClient, models}
import ca.pigscanfly.models.{LoginCredentials, MessageDelivery, MessagePost, MessageRetrieval}
import ca.pigscanfly.proto.MessageDataPB.{Message, MessageDataPB, Protocol}
import ca.pigscanfly.util.Constants._
import ca.pigscanfly.util.{ProtoUtils, Validations}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

case class SwarmService(twilioService: TwilioService)(userDAO: UserDAO, swarmMessageClient: SwarmMessageClient)
  extends SprayJsonSupport
    with Validations
    with ProtoUtils
    with LazyLogging {


  implicit val timeout: Timeout = Timeout(3.seconds)

  /**
   * This method retrieves messages from Swarm satellite by performing three operations:
   * Log in the Swarm Satellite
   * Retrieve messages from the Swarm Satellite using cookies from the response of login
   * Send acknowledgement for the retrieved messages
   *
   * @return Future[List[GetMessage]]
   *         GetMessage contains (packetId, deviceType, deviceId, deviceName, dataType, userApplicationId, len, data, ackPacketId, status, hiveRxTime) of the  retrieved message
   */
  def getMessages(getMessageActor:ActorRef): Future[MessageRetrieval] = {
    swarmLogin(LoginCredentials(swarmUserName, swarmPassword),getMessageActor).flatMap { cookies: Seq[HttpHeader] =>
      val messagesFut = (getMessageActor ? GetMessage(s"$SwarmBaseUrl/hive/api/v1/messages", cookies.toList)).mapTo[MessageRetrieval]

      messagesFut.map { messages =>
        messages.messageResponse.foreach { message: models.GetMessage =>
          getMessageActor ! MessageAck(s"$SwarmBaseUrl/hive/api/v1/messages/rxack", message.ackPacketId, cookies.toList)
        }
      }
      messagesFut
    }
  }

  /**
   * This method retrieves phone_number or email on the basis of device_id
   *
   * @param deviceId : user's device_id
   * @return Future[GetPhoneOrEmailSuccess]:
   *         GetPhoneOrEmailSuccess contains phone_number and email
   */
  def getPhoneOrEmailFromDeviceId(deviceId: Long,getMessageActor:ActorRef): Future[GetPhoneOrEmailSuccess] = {
    (getMessageActor ? GetEmailOrPhoneFromDeviceId(deviceId)).mapTo[GetPhoneOrEmailSuccess]
  }

  /**
   * This method sends messages to Swarm Satellite by performing listed operations:
   * Log in the Swarm Satellite
   * Fetch device_id from email or phone_number of the sender
   * Validate sender's subscription on the basis of device_id
   * Detect receiver's destination i.e. email or phone_number
   * Send message to the Swarm Satellite
   *
   * @param from : email or phone_number of the sender
   * @param to   : email or phone_number of the receiver
   * @param data : message to be sent
   * @return Future[MessageDelivery]:
   *         MessageDelivery: contains packetId and status of the sent message
   */
  def postMessages(from: String, to: String, data: String, sendMessageActor: ActorRef, getMessageActor: ActorRef): Future[MessageDelivery] = {
    swarmLogin(LoginCredentials(swarmUserName, swarmPassword),getMessageActor).flatMap { cookies: Seq[HttpHeader] =>
      if (validateEmailPhone(from)) {
        ask(sendMessageActor, GetDeviceIdFromEmailOrPhone(from)).flatMap {
          case response: GetDeviceId =>
            response.deviceId.fold(throw new Exception(s"Device is not present for phone number $from")) { deviceId =>
              ask(sendMessageActor, CheckSubscription(deviceId)).flatMap {
                case response: CheckDeviceSubscription =>
                  response.isDisabled.fold(throw new Exception(s"Couldn't found subscription details of device ID $deviceId")) { isDeviceDisabled =>
                    if (!isDeviceDisabled) {
                      val sourceDestination = detectSourceDestination(to)
                      val msg = sourceDestination match {
                        case EMAIl =>
                          Message(data, to, Protocol.values(1))
                        case SMS =>
                          Message(data, to, Protocol.values(2))
                        case UNKOWN =>
                          throw new Exception("Invalid Source")
                      }
                      //TODO Gather msg in time window
                      val messageDataPB: MessageDataPB = MessageDataPB(1, Seq(msg), false)
                      val messagePost = MessagePost(1, deviceId, 1, encodePostMessage(messageDataPB))
                      (sendMessageActor ? PostMessageCommand(s"$SwarmBaseUrl/hive/api/v1/messages", messagePost, cookies.toList)).mapTo[MessageDelivery].map {
                        postMessageResponse =>
                          val messageHistory = MessageHistory(deviceId, response.customerId.getOrElse(""), to, sourceDestination, "POST", postMessageResponse.packetId)
                          saveMessageHistory(messageHistory, getMessageActor)
                          postMessageResponse
                      }
                    } else {
                      throw new Exception(s"[Device ID Disabled] Can't send message from device Id $deviceId")
                    }
                  }
                case ex => throw new Exception(s"Got exception while checking Subscription for device ID $deviceId ex:: $ex")
              }
            }
          case ex => throw new Exception(s"Got exception while getting Device ID for phone number $from ex:: $ex")
        }
      } else {
        throw new Exception(s"Message received from $from i.e. is not a phone number")
      }
    }
  }

  /**
   * This method is to logged in the Swarm Satellite using swarm satellite's request url, username and password
   *
   * @param loginCredentials : request contains Swarm's username and password
   * @return Future[Seq[HttpHeader]]:
   *         If login is successful headers will be retrieved
   */
  def swarmLogin(loginCredentials: LoginCredentials, getMessageActor:ActorRef): Future[Seq[HttpHeader]] = {
    (getMessageActor ? SwarmLogin(s"$SwarmBaseUrl/login", loginCredentials)).mapTo[Seq[HttpHeader]]
  }

  def saveMessageHistory(messageHistory: MessageHistory, getMessageActor: ActorRef): Future[Status] = {
    logger.info(s"SwarmService: Sending request to save message history $messageHistory")
    (getMessageActor ? SaveMessageHistory(messageHistory)).mapTo[Status]
  }

}
