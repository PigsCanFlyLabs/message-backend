package ca.pigscanfly

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpecLike}

import scala.language.postfixOps

class SwarmStartTest extends TestKit(ActorSystem("test"))
  with MustMatchers
  with WordSpecLike
  with ScalaFutures
  with MockFactory
  with BeforeAndAfterAll {

//  val swarmMessageClient = new SwarmMessageClient with MockClientHandler {
//    override implicit def actorSystem: ActorSystem = system
//
//    override implicit def executionContext: ExecutionContext = ExecutionContext.Implicits.global
//  }
//
//  val cookieHeader = akka.http.scaladsl.model.headers.Cookie("JSESSIONID", "B120DCEBC05C9F6CE3FBCA259356C17E")
//  val json =
//    """[
//      |{
//      |"packetId": 0,
//      |"deviceType": 0,
//      |"deviceId": 0,
//      |"deviceName": "string",
//      |"dataType": 0,
//      |"userApplicationId": 0,
//      |"len": 0,
//      |"data": "VGhpcyBpcyBhIGJhc2g2NCBlbmNvZGVkIHN0cmluZw==",
//      |"ackPacketId": 0,
//      |"status": 0,
//      |"hiveRxTime": "2021-12-26T07:44:26.374Z"
//      |}
//      |]""".stripMargin
//
//  val ackResponseMock =
//    """{
//      |  "packetId": 0,
//      |  "status": "OK"
//      |}""".stripMargin
//
//  val requestMessage =
//    """{
//      |  "deviceType" : 1,
//      |  "deviceId" : 1,
//      |  "userApplicationId" : 1234,
//      |  "data" : "Some Message"
//      |}
//      |""".stripMargin
//
//  trait MockClientHandler extends HttpClient {
//    val mock = mockFunction[HttpRequest, Future[HttpResponse]]
//
//    override def sendRequest(httpRequest: HttpRequest)(implicit actorSystem: ActorSystem): Future[HttpResponse] =
//      mock(httpRequest)
//  }
//
//
//  "Swarm Client" should {
//    "Get Messages" in {
//      swarmMessageClient.mock
//        .expects(HttpRequest(uri = s"$SwarmBaseUrl/hive/api/v1/messages", headers = List(cookieHeader)))
//        .returning(Future.successful(HttpResponse(entity = HttpEntity(ByteString(json)))))
//      val response = swarmMessageClient.getMessages(s"$SwarmBaseUrl/hive/api/v1/messages", List(cookieHeader))
//
//      val result = for {
//        messages <- response
//      } yield {
//        println("messages" + messages)
//        messages
//      }
//      Thread.sleep(5000) //TODO REMOVE THIS THREAD SLEEP
//      assert(result.toString === Future.successful(MessageRetrieval(List(Message(0, 0, 0, "string", 0, 0, 0, "This is a bash64 encoded string", 0, 0, "2021-12-26T07:44:26.374Z")))).toString)
//    }
//
//    "messageAckSuccess" in {
//      swarmMessageClient.mock
//        .expects(HttpRequest(
//          method = HttpMethods.POST,
//          uri = s"$SwarmBaseUrl/hive/api/v1/messages/rxack/0",
//          headers = List(cookieHeader)
//        ))
//        .returning(Future.successful(HttpResponse(entity = HttpEntity(ByteString(ackResponseMock)))))
//
//      val response = swarmMessageClient.ackMessage(s"$SwarmBaseUrl/hive/api/v1/messages/rxack", 0, List(cookieHeader))
//      Thread.sleep(5000) //TODO REMOVE THIS THREAD SLEEP
//      for {
//        resp <- response
//      } yield {
//        println("ackResponse: " + resp)
//        assert(resp === MessageDelivery(0, "string"))
//      }
//    }
//
//    "PostSuccess" in {
//      swarmMessageClient.mock
//        .expects(HttpRequest(
//          method = HttpMethods.POST,
//          uri = s"$SwarmBaseUrl/hive/api/v1/messages",
//          headers = List(cookieHeader),
//          entity = HttpEntity(ContentTypes.`application/json`, MessagePost(1, 1, 1234, "Some Message").asJson.toString),
//        ))
//        .returning(Future.successful(HttpResponse(entity = HttpEntity(ByteString(ackResponseMock)))))
//
//      val response = swarmMessageClient.sendMessage(s"$SwarmBaseUrl/hive/api/v1/messages", MessagePost(deviceType = 1, deviceId = 1, userApplicationId = 1234, data = "Some Message"), List(cookieHeader))
//      Thread.sleep(5000) //TODO REMOVE THIS THREAD SLEEP
//      for {
//        resp <- response
//      } yield {
//        println("postResponse: " + resp)
//        assert(resp === MessageDelivery(0, "OK"))
//      }
//    }
//  }
}


