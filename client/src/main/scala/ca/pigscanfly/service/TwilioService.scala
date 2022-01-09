package ca.pigscanfly.service

import com.twilio.`type`.PhoneNumber
import com.twilio.rest.api.v2010.account.Message


class TwilioService {
  def sendToTwilio(to: String, from: String, data: String): Unit = {
    Message.creator(new PhoneNumber(to), new PhoneNumber(from), data).create()
  }
}
