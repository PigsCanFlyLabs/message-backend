package ca.pigscanfly.sendgrid

import ca.pigscanfly.configs.Constants.SendgridSecretKey
import com.sendgrid
import com.sendgrid._
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects._

trait SendGridEmailer {

  def sendDummyMail() {
    val from = new Email("jashangoyal96@gmail.com")
    val to = new Email("kumar.driviz@gmail.com")

    val subject = "Sending with Twilio SendGrid"
    val content = new Content("text/html", "Test Mesage")

    val mail = new Mail(from, subject, to, content)

    val sg = new SendGrid(SendgridSecretKey)
    val request = new Request

    request.setMethod(Method.POST)
    request.setEndpoint("mail/send")
    request.setBody(mail.build)

    val response: sendgrid.Response = sg.api(request)

    System.out.println(response.getStatusCode)
    System.out.println(response.getHeaders)
    System.out.println(response.getBody)
  }


}
