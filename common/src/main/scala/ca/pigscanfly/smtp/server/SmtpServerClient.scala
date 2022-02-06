package ca.pigscanfly.smtp.server

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import ca.pigscanfly.configs.Constants.{SmtpAddress, SmtpPort}
import pl.jozwik.smtp.client.StreamClient
import pl.jozwik.smtp.server._
import pl.jozwik.smtp.server.consumer.LogConsumer
import pl.jozwik.smtp.util.{ConsumedResult, Mail, _}

import scala.concurrent.Future
import scala.concurrent.duration._

abstract class SmtpServerClient(implicit val system: ActorSystem) {

  implicit val materializer = ActorMaterializer()
  protected final lazy val clientStream: StreamClient = new StreamClient(SmtpAddress, SmtpPort)
  protected final val configuration = Configuration(SmtpPort, maxSize, readTimeout)
  protected final val server: StreamServer = StreamServer(consumer, configuration, addressHandler)
  protected lazy val address: SocketAddress = SocketAddress(SmtpAddress, SmtpPort)
  private val defaultMaxSize = 1024

  def consumer(mail: Mail): Future[ConsumedResult] = LogConsumer.consumer(mail)

  protected def readTimeout: FiniteDuration = 30 seconds

  protected def maxSize: Int = defaultMaxSize

  protected def addressHandler: AddressHandler = NopAddressHandler


}
