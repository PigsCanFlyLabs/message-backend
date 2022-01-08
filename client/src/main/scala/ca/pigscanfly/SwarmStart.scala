package ca.pigscanfly

import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.Cookie
import ca.pigscanfly.httpClient.HttpClient

import scala.concurrent.ExecutionContext

object SwarmStart {

  implicit val system: ActorSystem = ActorSystem("Swarm-Start")
  implicit val executionContext: ExecutionContext = system.dispatcher


  val swarmMessageClient = new SwarmMessageClient() with HttpClient {
    override implicit def actorSystem: ActorSystem = system

    override implicit def executionContext: ExecutionContext = actorSystem.dispatcher
  }
  val cookieHeader: Cookie = Cookie("JSESSIONID", "B120DCEBC05C9F6CE3FBCA259356C17E")

}
