package ca.pigscanfly

import akka.actor.ActorSystem
import ca.pigscanfly.httpClient.ClientHandler
import ca.pigscanfly.models.MessagePost

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object SwarmStart extends App {

  implicit val system: ActorSystem = ActorSystem("Swarm-Start")
  implicit val executionContext: ExecutionContext = system.dispatcher

  val newMessage = new SwarmMessageClient() with ClientHandler {
    override implicit def actorSystem: ActorSystem = system

    override implicit def executionContext: ExecutionContext = actorSystem.dispatcher
  }

  newMessage.getMessages("https://bumblebee.hive.swarm.space/hive/api/v1/messages").onComplete { res =>
    res match {
      case Success(value) =>
        println(value)
      case Failure(ex) => throw new Exception(ex.getMessage)
    }
    newMessage.shutDown()
  }

  newMessage.postMessage("https://bumblebee.hive.swarm.space/hive/api/v1/messages", new MessagePost(deviceType = 0, deviceId = 0, userApplicationId = 0, data = "un-encoded data")).onComplete { res =>
    res match {
      case Success(value) =>
        println(value)
      case Failure(ex) => throw new Exception(ex.getMessage)
    }
    newMessage.shutDown()
  }

  newMessage.ackMessage("https://bumblebee.hive.swarm.space/hive/api/v1/messages/rxack", 0).onComplete { res =>
    res match {
      case Success(value) =>
        println(value)
      case Failure(ex) => throw new Exception(ex.getMessage)
    }
    newMessage.shutDown()
  }
}
