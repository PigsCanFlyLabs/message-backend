package ca.pigscanfly

import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.Cookie
import ca.pigscanfly.configs.Constants.SwarmBaseUrl
import ca.pigscanfly.httpClient.ClientHandler
import ca.pigscanfly.models.MessagePost

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object SwarmStart {

  implicit val system: ActorSystem = ActorSystem("Swarm-Start")
  implicit val executionContext: ExecutionContext = system.dispatcher

  val swarmMessageClient = new SwarmMessageClient() with ClientHandler {
    override implicit def actorSystem: ActorSystem = system

    override implicit def executionContext: ExecutionContext = actorSystem.dispatcher
  }
  val cookieHeader = Cookie("JSESSIONID", "B120DCEBC05C9F6CE3FBCA259356C17E")

  swarmMessageClient.getMessages(s"$SwarmBaseUrl/hive/api/v1/messages", List(cookieHeader)).onComplete { res =>
    res match {
      case Success(value) =>
        println(value)
      case Failure(ex) => throw new Exception(ex.getMessage)
    }
    swarmMessageClient.shutDown()
  }

  swarmMessageClient.sendMessage(s"$SwarmBaseUrl/hive/api/v1/messages", MessagePost(deviceType = 1, deviceId = 1, userApplicationId = 1234, data = "Some Message"), List(cookieHeader)).onComplete { res =>
    res match {
      case Success(value) =>
        println(value)
      case Failure(ex) => throw new Exception(ex.getMessage)
    }
    swarmMessageClient.shutDown()
  }

  swarmMessageClient.ackMessage(s"$SwarmBaseUrl/hive/api/v1/messages/rxack", 0, List(cookieHeader)).onComplete { res =>
    res match {
      case Success(value) =>
        println(value)
      case Failure(ex) => throw new Exception(ex.getMessage)
    }
    swarmMessageClient.shutDown()
  }
}
