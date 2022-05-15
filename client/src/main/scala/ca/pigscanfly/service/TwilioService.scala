package ca.pigscanfly.service

import com.twilio.`type`.PhoneNumber
import com.twilio.rest.api.v2010.account.Message
import com.typesafe.scalalogging.LazyLogging


class TwilioService extends LazyLogging {

  /**
   * This method is responsible to sends a message using Twilio service
   *
   * @param to   : receiver of the message
   * @param from : sender of the message
   * @param data : message to be sent
   */
  def sendToTwilio(to: String, from: String, data: String): Unit = {
    try {
      Message.creator(new PhoneNumber(to), new PhoneNumber(from), data).create()
      logger.info(s"TwilioService: successfully sent message: |$data| from: $from to: $to via Twilio.")
    } catch {
      case exception: Exception =>
        logger.info(s"TwilioService: Failed to send message via Twilio. Exception occurred: $exception.")
    }
  }
}
