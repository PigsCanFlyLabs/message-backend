package ca.pigscanfly.service

import ca.pigscanfly.configs.Constants.{AccountSID, AuthToken}
import com.twilio.Twilio
import com.twilio.`type`.PhoneNumber
import com.twilio.rest.api.v2010.account.Message

//object TwilioService{
//  val twilioObject = Twilio.init(AccountSID, AuthToken)
//}

class TwilioService {

  // TODO: Correct this to create a single object and then inject
  Twilio.init(AccountSID, AuthToken)
  def sendToTwilio(reciever:String, sender:String, data:String): Unit ={
    Message.creator(new PhoneNumber(sender), new PhoneNumber(reciever), data).create()
  }

}
