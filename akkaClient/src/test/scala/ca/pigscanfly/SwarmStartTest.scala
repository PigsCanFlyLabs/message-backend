package ca.pigscanfly

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse}
import akka.testkit.TestKit
import akka.util.ByteString
import ca.pigscanfly.configs.Constants.SwarmBaseUrl
import ca.pigscanfly.httpClient.HttpClient
import ca.pigscanfly.models.MessagePost.encoder
import ca.pigscanfly.models.{Message, MessageDelivery, MessagePost, MessageRetrieval}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpecLike}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import io.circe.syntax._

class SwarmStartTest extends TestKit(ActorSystem("test"))
  with MustMatchers
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

  val ackResponseMock =
    """{
      |  "packetId": 0,
      |  "status": "OK"
      |}""".stripMargin

  val requestMessage =
    """{
      |  "deviceType" : 1,
      |  "deviceId" : 1,
      |  "userApplicationId" : 1234,
      |  "data" : "Some Message"
      |}
      |""".stripMargin

  trait MockClientHandler extends HttpClient {
    val mock = mockFunction[HttpRequest, Future[HttpResponse]]

    override def sendRequest(httpRequest: HttpRequest)(implicit actorSystem: ActorSystem): Future[HttpResponse] =
      mock(httpRequest)
  }


  "Swarm Client" should {
    "Get Messages" in {
      swarmMessageClient.mock
        .expects(HttpRequest(uri = s"$SwarmBaseUrl/hive/api/v1/messages", headers = List(cookieHeader)))
        .returning(Future.successful(HttpResponse(entity = HttpEntity(ByteString(json)))))
      val response = swarmMessageClient.getMessages(s"$SwarmBaseUrl/hive/api/v1/messages")
      Thread.sleep(5000) //TODO REMOVE THIS THREAD SLEEP
      for {
        messages <- response
      } yield {
        println("messages" + messages)
        assert(messages === MessageRetrieval(List(Message(0, 0, 0, "string", 0, 0, 0, "string", 0, 0, "string"))))
      }
    }

    "messageAckSuccess" in {
      swarmMessageClient.mock
        .expects(HttpRequest(
          method = HttpMethods.POST,
          uri = s"$SwarmBaseUrl/hive/api/v1/messages/rxack/0",
          headers = List(cookieHeader)
        ))
        .returning(Future.successful(HttpResponse(entity = HttpEntity(ByteString(ackResponseMock)))))

      val response = swarmMessageClient.ackMessage(s"$SwarmBaseUrl/hive/api/v1/messages/rxack", 0)
      Thread.sleep(5000) //TODO REMOVE THIS THREAD SLEEP
      for {
        resp <- response
      } yield {
        println("ackResponse: " + resp)
        assert(resp === MessageDelivery(0, "string"))
      }
    }

    "PostSuccess" in {
      swarmMessageClient.mock
        .expects(HttpRequest(
          method = HttpMethods.POST,
          uri = s"$SwarmBaseUrl/hive/api/v1/messages",
          headers = List(cookieHeader),
          entity = HttpEntity(ContentTypes.`application/json`, MessagePost(1, 1, 1234, "Some Message").asJson.toString),
        ))
        .returning(Future.successful(HttpResponse(entity = HttpEntity(ByteString(ackResponseMock)))))

      val response = swarmMessageClient.postMessage(s"$SwarmBaseUrl/hive/api/v1/messages", MessagePost(deviceType = 1, deviceId = 1, userApplicationId = 1234, data = "Some Message"))
      Thread.sleep(5000) //TODO REMOVE THIS THREAD SLEEP
      for {
        resp <- response
      } yield {
        println("postResponse: " + resp)
        assert(resp === MessageDelivery(0, "OK"))
      }
    }
  }
}


