package ca.pigscanfly.service

import com.twilio.`type`.PhoneNumber
import com.twilio.rest.api.v2010.account.Message
import com.typesafe.scalalogging.LazyLogging


class TwilioService extends LazyLogging {
  def sendToTwilio(to: String, from: String, data: String): Unit = {
    try {
      Message.creator(new PhoneNumber(to), new PhoneNumber(from), data).create()
      logger.info(s"TwilioService: successfully sent message: |$data| from: $from to: $to via Twillio.")
    }catch{
      case exception: Exception=>
        logger.info(s"TwilioService: Failed to send message via Twillio. Excption occured: $exception.")
    }
  }
}
