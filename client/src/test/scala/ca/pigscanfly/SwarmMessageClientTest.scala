package ca.pigscanfly

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Cookie, `Content-Type`}
import akka.testkit.TestKit
import akka.util.ByteString
import ca.pigscanfly.configs.Constants.SwarmBaseUrl
import ca.pigscanfly.httpClient.HttpClient
import ca.pigscanfly.models._
import io.circe.syntax.EncoderOps
import org.scalamock.function.MockFunction1
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{AsyncWordSpecLike, BeforeAndAfterAll, MustMatchers}

import scala.concurrent.{ExecutionContext, Future}

class SwarmStartTest extends TestKit(ActorSystem("test")) with AsyncWordSpecLike
  with MustMatchers
  with ScalaFutures
  with MockFactory
  with BeforeAndAfterAll {

  val swarmMessageClient: SwarmMessageClient with MockClientHandler = new SwarmMessageClient with MockClientHandler {
    override implicit def actorSystem: ActorSystem = system

    override implicit def executionContext: ExecutionContext = ExecutionContext.Implicits.global
  }

  val cookieHeader: Cookie = akka.http.scaladsl.model.headers.Cookie("JSESSIONID", "B120DCEBC05C9F6CE3FBCA259356C17E")
  val contentTypeHeader: `Content-Type` = akka.http.scaladsl.model.headers.`Content-Type`(ContentTypes.`application/json`)
  val json: String =
    """[
      |{
      |"packetId": 0,
      |"deviceType": 0,
      |"deviceId": 0,
      |"deviceName": "string",
      |"dataType": 0,
      |"userApplicationId": 0,
      |"len": 0,
      |"data": "ChFUaGlzIGlzIGEgbWVzc2FnZQ==",
      |"ackPacketId": 0,
      |"status": 0,
      |"hiveRxTime": "2021-12-26T07:44:26.374Z"
      |}
      |]""".stripMargin

  val ackResponseMock: String =
    """{
      |  "packetId": 0,
      |  "status": "OK"
      |}""".stripMargin

  val requestMessage: String =
    """{
      |  "deviceType" : 1,
      |  "deviceId" : 1,
      |  "userApplicationId" : 1234,
      |  "data" : "CgxTb21lIE1lc3NhZ2U="
      |}
      |""".stripMargin

  trait MockClientHandler extends HttpClient {
    val mock: MockFunction1[(String, List[HttpHeader], String, HttpMethod), Future[HttpResponse]] = mockFunction[(String, List[HttpHeader], String, HttpMethod), Future[HttpResponse]]

    override def sendRequest(finalApiPath: String, headers: List[HttpHeader], message: String, requestMethod: HttpMethod)(implicit actorSystem: ActorSystem): Future[HttpResponse] =
      mock(finalApiPath, headers, message, requestMethod)
  }

  "Swarm Client" should {
    "Get Messages" in {
      swarmMessageClient.mock
        .expects(s"$SwarmBaseUrl/hive/api/v1/messages", List(cookieHeader, contentTypeHeader), "", HttpMethods.GET)
        .returning(Future.successful(HttpResponse(status = StatusCodes.OK, headers = List(contentTypeHeader), entity = HttpEntity(ContentTypes.`application/json`, json))))
      swarmMessageClient.getMessages(s"$SwarmBaseUrl/hive/api/v1/messages", List(cookieHeader, contentTypeHeader)).map { response =>
        assert(response.toString === Future.successful(MessageRetrieval(List(GetMessage(0, 0, 0, "string", 0, 0, 0, "This is a message", 0, 0, "2021-12-26T07:44:26.374Z")))).toString)
      }
    }

    "messageAckSuccess" in {
      swarmMessageClient.mock
        .expects(s"$SwarmBaseUrl/hive/api/v1/messages/rxack/0", List(cookieHeader), "", HttpMethods.POST)
        .returning(Future.successful(HttpResponse(entity = HttpEntity(ackResponseMock))))

      swarmMessageClient.ackMessage(s"$SwarmBaseUrl/hive/api/v1/messages/rxack", 0, List(cookieHeader)).map { response =>
        assert(response === MessageDelivery(0, "string"))
      }
    }

    "PostSuccess" in {
      val requestBody = MessagePost(deviceType = 1, deviceId = 1, userApplicationId = 1234, data = "Some Message")
      swarmMessageClient.mock
        .expects(s"$SwarmBaseUrl/hive/api/v1/messages", List(cookieHeader), requestBody.copy(data = "CgxTb21lIE1lc3NhZ2U=").asJson.toString(), HttpMethods.POST)
        .returning(Future.successful(HttpResponse(entity = HttpEntity(ByteString(ackResponseMock)))))

      swarmMessageClient.sendMessage(s"$SwarmBaseUrl/hive/api/v1/messages", requestBody, List(cookieHeader)).map { resp =>
        assert(resp === MessageDelivery(0, "OK"))
      }
    }
    //    "PostSuccess" in {
    //      val requestBody = MessagePost(deviceType = 1, deviceId = 1, userApplicationId = 1234, data = "Some Message")
    //      swarmMessageClient.mock
    //        .expects(s"$SwarmBaseUrl/hive/api/v1/messages", List(cookieHeader), requestBody.copy(data = "CgxTb21lIE1lc3NhZ2U=").asJson.toString(), HttpMethods.POST)
    //        .returning(Future.successful(HttpResponse(entity = HttpEntity(ByteString(ackResponseMock)))))
    //
    //      swarmMessageClient.sendMessage(s"$SwarmBaseUrl/hive/api/v1/messages", requestBody, List(cookieHeader)).map{ resp =>
    //        println(s"\n\nresp\n${resp}\n\n\n")
    //        assert(resp === MessageDelivery(0, "OK"))
    //      }
    //    }

    "LoginSuccess" in {
      val requestBody = LoginCredentials("username", "password")
      swarmMessageClient.mock
        .expects(s"$SwarmBaseUrl/login", List(cookieHeader), requestBody.asJson.toString(), HttpMethods.GET)
        .returning(Future.successful(HttpResponse(headers = List(cookieHeader))))

      val response = swarmMessageClient.login(s"$SwarmBaseUrl/login", requestBody)
      Thread.sleep(5000)

      assert(response === MessageDelivery(0, "OK"))
    }
    //    "LoginSuccess" in {
    //      val requestBody = LoginCredentials("username","password")
    //      swarmMessageClient.mock
    //        .expects(s"$SwarmBaseUrl/login", List(cookieHeader), requestBody.asJson.toString(), HttpMethods.GET)
    //        .returning(Future.successful(HttpResponse(headers = List(cookieHeader))))
    //
    //      val response = swarmMessageClient.login(s"$SwarmBaseUrl/login", requestBody)
    //      Thread.sleep(5000)
    //
    //      assert(response === MessageDelivery(0, "OK"))
    //    }
  }
}


