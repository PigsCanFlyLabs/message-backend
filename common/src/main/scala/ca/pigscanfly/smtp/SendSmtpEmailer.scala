package ca.pigscanfly.smtp

import akka.actor.ActorSystem
import ca.pigscanfly.smtp.server.SmtpServerClient
import pl.jozwik.smtp.client.FailedResult
import pl.jozwik.smtp.util.{EmailWithContent, Mail, MailAddress}

import scala.concurrent.ExecutionContext.Implicits.global

class SendSmtpEmailer(implicit val actorSystem: ActorSystem) extends SmtpServerClient() {

  def sendSmtMail(from: String, to: String, text: String): Unit = {

    //TODO REPLACE OUTLOOK with SMPT server domain
    val fromAddress = MailAddress(from, "@outlook.com")
    val toAddress = MailAddress(to, "@outlook.com")
    val mail = Mail(fromAddress, Seq(toAddress), EmailWithContent.txtOnly(Seq.empty, Seq.empty, "New Message from Swarm!", text))


    clientStream.sendMail(mail).recover { case e =>
      FailedResult(e.getMessage)
    }
  }

}
