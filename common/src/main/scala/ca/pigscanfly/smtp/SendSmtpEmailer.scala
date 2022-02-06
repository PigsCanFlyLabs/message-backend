package ca.pigscanfly.smtp

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import ca.pigscanfly.configs.Constants.{SmtpAddress, SmtpPort}
import pl.jozwik.smtp.client.{FailedResult, StreamClient}
import pl.jozwik.smtp.util.{EmailWithContent, Mail, MailAddress, SocketAddress}

import scala.concurrent.ExecutionContext

trait SendSmtpEmailer {

  def sendSmtMail(from:String, to:String, text:String): Unit = {

    val address = SmtpAddress
    val host = SmtpPort

    val serverAddress = SocketAddress(address, host)

    val fromAddress = MailAddress(from, "@outlook.com")
    val toAddress = MailAddress(to, "@outlook.com")
    val mail = Mail(fromAddress, Seq(toAddress), EmailWithContent.txtOnly(Seq.empty, Seq.empty, "New Message from Swarm!", text))


    implicit val system = ActorSystem("name")
    implicit val executionContext: ExecutionContext = system.dispatcher
    implicit val materializer = ActorMaterializer()
    val client = new StreamClient(address, 8080)

    client.sendMail(mail).recover { case e =>
      FailedResult(e.getMessage)
    }
  }

}
