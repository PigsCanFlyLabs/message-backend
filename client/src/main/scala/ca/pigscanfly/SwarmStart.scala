package ca.pigscanfly

import akka.actor.{ActorSystem, Scheduler}
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.Cookie
import ca.pigscanfly.Application.system
import ca.pigscanfly.configs.Constants.SwarmBaseUrl
import ca.pigscanfly.httpClient.ClientHandler
import ca.pigscanfly.models.MessagePost
import ca.pigscanfly.service.{SwarmService, TwilioService}

import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}
import akka.http.scaladsl.model._

object SwarmStart {

  implicit val system: ActorSystem = ActorSystem("Swarm-Start")
  implicit val executionContext: ExecutionContext = system.dispatcher
  val scheduler: Scheduler = system.scheduler


  val swarmMessageClient = new SwarmMessageClient() with ClientHandler {
    override implicit def actorSystem: ActorSystem = system

    override implicit def executionContext: ExecutionContext = actorSystem.dispatcher
  }
  val cookieHeader: Cookie = Cookie("JSESSIONID", "B120DCEBC05C9F6CE3FBCA259356C17E")

//  val twilioService = new TwilioService()
//  val swarmService = new SwarmService(twilioService)(system)

//
//  //TODO: Experimental
//  val header = headers.Cookie("name", "string")
//  val request = HttpRequest(
//    method = HttpMethods.GET,
//    uri = "FINAL_API_PATH",
//    headers = List(header)
//  )
//  val task = new Runnable { def run() { swarmService.getMessages(request) } }
//
//  scheduler.scheduleWithFixedDelay(
//    initialDelay = Duration(5, TimeUnit.SECONDS),
//    delay = Duration(5, TimeUnit.MINUTES))(task)

//  swarmMessageClient.getMessages(s"$SwarmBaseUrl/hive/api/v1/messages", List(cookieHeader)).onComplete { res =>
//    res match {
//      case Success(value) =>
//        println(value)
//      case Failure(ex) => throw new Exception(ex.getMessage)
//    }
//    swarmMessageClient.shutDown()
//  }
//
//  swarmMessageClient.sendMessage(s"$SwarmBaseUrl/hive/api/v1/messages", MessagePost(deviceType = 1, deviceId = 1, userApplicationId = 1234, data = "Some Message"), List(cookieHeader)).onComplete { res =>
//    res match {
//      case Success(value) =>
//        println(value)
//      case Failure(ex) => throw new Exception(ex.getMessage)
//    }
//    swarmMessageClient.shutDown()
//  }
//
//  swarmMessageClient.ackMessage(s"$SwarmBaseUrl/hive/api/v1/messages/rxack", 0, List(cookieHeader)).onComplete { res =>
//    res match {
//      case Success(value) =>
//        println(value)
//      case Failure(ex) => throw new Exception(ex.getMessage)
//    }
//    swarmMessageClient.shutDown()
//  }
}
