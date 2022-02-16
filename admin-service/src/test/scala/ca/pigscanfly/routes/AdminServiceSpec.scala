package ca.pigscanfly.routes

import akka.actor.ActorRef
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.ActorMaterializer
import ca.pigscanfly.actor.AdminActor
import ca.pigscanfly.cache.RoleAuthorizationCache
import ca.pigscanfly.dao.{AdminDAO, UserDAO}
import org.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import slick.jdbc.H2Profile

import scala.concurrent.Future
import scala.concurrent.duration.{FiniteDuration, _}

class AdminServiceSpec extends WordSpec with Matchers with ScalatestRouteTest
  with AdminService with MockitoSugar {

  val driver = H2Profile

  import driver.api.Database

  implicit val db: driver.api.Database = mock[Database]
  implicit val schema: String = ""

  implicit val userDAO: UserDAO = mock[UserDAO]
  implicit val adminDAO: AdminDAO = mock[AdminDAO]

  override implicit val materializer: ActorMaterializer = ActorMaterializer()

  val futureAwaitTime: FiniteDuration = 10.minute

  val token: String = createJwtTokenWithRole("user@gmail.com", "admin")
  val adminAuth: Authorization = Authorization(OAuth2BearerToken(token))
  val userToken: String = createJwtTokenWithRole("user@gmail.com", "user")
  val userAuth: Authorization = Authorization(OAuth2BearerToken(userToken))
  implicit val futureAwaitDuration: FiniteDuration =
    FiniteDuration(futureAwaitTime.length, futureAwaitTime.unit)

  val adminActor: ActorRef = system.actorOf(
    AdminActor
      .props(adminDAO, userDAO),
    "adminActor")

  val route: Route = adminUserRoutes(adminActor)

  override def getUserDetails(command: ActorRef,
                              deviceId: Long): Future[HttpResponse] = Future.successful(HttpResponse.apply())

  "return OK" in {
    when(adminDAO.getResourcePermissions()) thenReturn Future((Seq(), Seq()))
    Get("/admin/get-user-details?deviceId=100").addHeader(adminAuth) ~> route ~> check {
      status shouldEqual StatusCodes.Unauthorized
    }
  }


  override implicit val routeCache: RoleAuthorizationCache = new RoleAuthorizationCache(adminDAO)

}
