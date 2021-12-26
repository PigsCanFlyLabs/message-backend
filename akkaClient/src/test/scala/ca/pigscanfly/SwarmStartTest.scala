package ca.pigscanfly

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse}
import akka.testkit.TestKit
import akka.util.ByteString
import ca.pigscanfly.httpClient.HttpClient
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpecLike}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class SwarmStartTest extends TestKit(ActorSystem("test")) with MustMatchers
  with WordSpecLike
  with ScalaFutures
  with MockFactory
  with BeforeAndAfterAll {

  val swarmMessageClient = new SwarmMessageClient with MockClientHandler {
    override implicit def actorSystem: ActorSystem = system

    override implicit def executionContext: ExecutionContext = ExecutionContext.Implicits.global
  }
  val cookieHeader = akka.http.scaladsl.model.headers.Cookie("JSESSIONID", "B120DCEBC05C9F6CE3FBCA259356C17E")
  val json =
    """[
      |{
      |"packetId": 0,
      |"deviceType": 0,
      |"deviceId": 0,
      |"deviceName": "string",
      |"dataType": 0,
      |"userApplicationId": 0,
      |"len": 0,
      |"data": "string",
      |"ackPacketId": 0,
      |"status": 0,
      |"hiveRxTime": "2021-12-26T07:44:26.374Z"
      |}
      |]""".stripMargin
  val aaaa = swarmMessageClient.getMessages("https://bumblebee.hive.swarm.space/hive/api/v1/messages").map(println)

  swarmMessageClient.mock
    .expects(HttpRequest(uri = "https://bumblebee.hive.swarm.space/hive/api/v1/messages", headers = List(cookieHeader)))
    .returning(Future.successful(HttpResponse(entity = HttpEntity(ByteString(json)))))

  trait MockClientHandler extends HttpClient {
    val mock = mockFunction[HttpRequest, Future[HttpResponse]]

    override def sendRequest(httpRequest: HttpRequest)(implicit actorSystem: ActorSystem): Future[HttpResponse] =
      mock(httpRequest)
  }

  Thread.sleep(30000)
  "test" should {
    "test" in {
      whenReady(swarmMessageClient.getMessages("https://bumblebee.hive.swarm.space/hive/api/v1/messages")) { res =>
        println(res)
        res must equal("New request handled")
      }
    }
  }
}
