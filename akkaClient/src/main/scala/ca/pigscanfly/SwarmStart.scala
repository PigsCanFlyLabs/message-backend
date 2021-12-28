package ca.pigscanfly

import akka.actor.ActorSystem
import ca.pigscanfly.configs.Constants.SwarmBaseUrl
import ca.pigscanfly.httpClient.ClientHandler
import ca.pigscanfly.models.MessagePost
import ca.pigscanfly.sendgrid.SendGridEmailer

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object SwarmStart extends App {

  implicit val system: ActorSystem = ActorSystem("Swarm-Start")
  implicit val executionContext: ExecutionContext = system.dispatcher

  val newMessage = new SwarmMessageClient() with ClientHandler {
    override implicit def actorSystem: ActorSystem = system

    override implicit def executionContext: ExecutionContext = actorSystem.dispatcher
  }

  newMessage.getMessages(s"$SwarmBaseUrl/hive/api/v1/messages").onComplete { res =>
    res match {
      case Success(value) =>
        println(value)
      case Failure(ex) => throw new Exception(ex.getMessage)
    }
    newMessage.shutDown()
  }

  newMessage.postMessage(s"$SwarmBaseUrl/hive/api/v1/messages", MessagePost(deviceType = 1, deviceId = 1, userApplicationId = 1234, data = "Some Message")).onComplete { res =>
    res match {
      case Success(value) =>
        println(value)
      case Failure(ex) => throw new Exception(ex.getMessage)
    }
    newMessage.shutDown()
  }

  newMessage.ackMessage(s"$SwarmBaseUrl/hive/api/v1/messages/rxack", 0).onComplete { res =>
    res match {
      case Success(value) =>
        println(value)
      case Failure(ex) => throw new Exception(ex.getMessage)
    }
    newMessage.shutDown()
  }
}
