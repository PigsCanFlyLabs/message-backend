package ca.pigscanfly.routes

import akka.actor.ActorRef
import akka.http.scaladsl.model.HttpMethods.{DELETE, POST}
import akka.http.scaladsl.model.HttpProtocols.`HTTP/1.0`
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.ActorMaterializer
import ca.pigscanfly.actor.AdminActor
import ca.pigscanfly.cache.RoleAuthorizationCache
import ca.pigscanfly.components._
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

  val superAdminToken: String = createJwtTokenWithRole("user@gmail.com", "super_admin")
  val superAdminAuth: Authorization = Authorization(OAuth2BearerToken(superAdminToken))
  val adminToken: String = createJwtTokenWithRole("user@gmail.com", "admin")
  val adminAuth: Authorization = Authorization(OAuth2BearerToken(adminToken))
  val userToken: String = createJwtTokenWithRole("user@gmail.com", "user")
  val userAuth: Authorization = Authorization(OAuth2BearerToken(userToken))
  implicit val futureAwaitDuration: FiniteDuration =
    FiniteDuration(futureAwaitTime.length, futureAwaitTime.unit)

  val adminActor: ActorRef = system.actorOf(
    AdminActor
      .props(adminDAO, userDAO),
    "adminActor")

  val route: Route = adminUserRoutes(adminActor)

  val rolesResourceAccessDB = Seq(RolesResourceAccessDB("admin", "admin_permissions", 1),
    RolesResourceAccessDB("super_admin", "super_admin_permissions,admin_permissions", 2))

  val resourcePermissionsDB = Seq(ResourcePermissionsDB("admin_permissions", "get-user-details,create-user,update-user,disable-user"),
    ResourcePermissionsDB("super_admin_permissions", "delete-user,create-admin-user"))

  override def getUserDetails(command: ActorRef,
                              deviceId: Long): Future[HttpResponse] = Future.successful(HttpResponse.apply())

  override def createAdmin(command: ActorRef,
                           admin: AdminLogin): Future[HttpResponse] = Future.successful(HttpResponse.apply())

  override def createUser(command: ActorRef,
                          user: User): Future[HttpResponse] = Future.successful(HttpResponse.apply())

  override def updateUser(command: ActorRef,
                          user: UpdateUserRequest): Future[HttpResponse] = Future.successful(HttpResponse.apply())

  override def disableUser(command: ActorRef,
                           request: DisableUserRequest): Future[HttpResponse] = Future.successful(HttpResponse.apply())

  override def deleteUser(command: ActorRef,
                          request: DeleteUserRequest): Future[HttpResponse] = Future.successful(HttpResponse.apply())

  override def adminLogin(command: ActorRef,
                          adminLogin: AdminLogin): Future[HttpResponse] = Future.successful(HttpResponse.apply())


  "return OK if super admin sends request" in {
    when(adminDAO.getResourcePermissions) thenReturn Future((rolesResourceAccessDB, resourcePermissionsDB))
    Get("/admin/get-user-details?deviceId=100").addHeader(superAdminAuth) ~> route ~> check {
      status shouldEqual StatusCodes.OK
    }

    HttpRequest(
      POST,
      uri = "/admin/create-admin-user",
      entity = HttpEntity(
        ContentTypes.`application/json`,
        """{
          |    "email": "email",
          |    "password": "password",
          |    "role":"admin"
          |}""".stripMargin),
      headers = List(superAdminAuth),
      protocol = `HTTP/1.0`) ~> route ~> check {
      status shouldBe StatusCodes.OK
    }

    HttpRequest(
      POST,
      uri = "/admin/create-user",
      entity = HttpEntity(
        ContentTypes.`application/json`,
        """{
          |    "deviceId": "10003",
          |    "phone": "9684356",
          |    "email": "email",
          |    "isDisabled":false
          |}""".stripMargin),
      headers = List(superAdminAuth),
      protocol = `HTTP/1.0`) ~> route ~> check {
      status shouldBe StatusCodes.OK
    }

    HttpRequest(
      POST,
      uri = "/admin/update-user",
      entity = HttpEntity(
        ContentTypes.`application/json`,
        """{
          |    "deviceId": "10003",
          |    "phone": "9684356",
          |    "email": "email",
          |    "isDisabled":false
          |}""".stripMargin),
      headers = List(superAdminAuth),
      protocol = `HTTP/1.0`) ~> route ~> check {
      status shouldBe StatusCodes.OK
    }

    HttpRequest(
      POST,
      uri = "/admin/disable-user",
      entity = HttpEntity(
        ContentTypes.`application/json`,
        """{
          |    "deviceId": "10036",
          |    "isDisabled":true
          |}""".stripMargin),
      headers = List(superAdminAuth),
      protocol = `HTTP/1.0`) ~> route ~> check {
      status shouldBe StatusCodes.OK
    }

    HttpRequest(
      DELETE,
      uri = "/admin/delete-user",
      entity = HttpEntity(
        ContentTypes.`application/json`,
        """{
          |    "deviceId": "10036"
          |}""".stripMargin),
      headers = List(superAdminAuth),
      protocol = `HTTP/1.0`) ~> route ~> check {
      status shouldBe StatusCodes.OK
    }
  }


  "return OK for login request" in {
    HttpRequest(
      POST,
      uri = "/admin/login",
      entity = HttpEntity(
        ContentTypes.`application/json`,
        """{
          |    "email": "email",
          |    "password": "password",
          |    "role":"admin"
          |}""".stripMargin),
      headers = List(),
      protocol = `HTTP/1.0`) ~> route ~> check {
      status shouldBe StatusCodes.OK
    }
  }

  "return OK if admin sends request" in {
    when(adminDAO.getResourcePermissions) thenReturn Future((rolesResourceAccessDB, resourcePermissionsDB))
    Get("/admin/get-user-details?deviceId=100").addHeader(adminAuth) ~> route ~> check {
      status shouldEqual StatusCodes.OK
    }


    HttpRequest(
      POST,
      uri = "/admin/create-user",
      entity = HttpEntity(
        ContentTypes.`application/json`,
        """{
          |    "deviceId": "10003",
          |    "phone": "9684356",
          |    "email": "email",
          |    "isDisabled":false
          |}""".stripMargin),
      headers = List(adminAuth),
      protocol = `HTTP/1.0`) ~> route ~> check {
      status shouldBe StatusCodes.OK
    }

    HttpRequest(
      POST,
      uri = "/admin/update-user",
      entity = HttpEntity(
        ContentTypes.`application/json`,
        """{
          |    "deviceId": "10003",
          |    "phone": "9684356",
          |    "email": "email"
          |}""".stripMargin),
      headers = List(adminAuth),
      protocol = `HTTP/1.0`) ~> route ~> check {
      status shouldBe StatusCodes.OK
    }

    HttpRequest(
      POST,
      uri = "/admin/disable-user",
      entity = HttpEntity(
        ContentTypes.`application/json`,
        """{
          |    "deviceId": "10036",
          |    "isDisabled":true
          |}""".stripMargin),
      headers = List(adminAuth),
      protocol = `HTTP/1.0`) ~> route ~> check {
      status shouldBe StatusCodes.OK
    }

  }


  "return Unauthorized if admin sends request" in {

    HttpRequest(
      DELETE,
      uri = "/admin/delete-user",
      entity = HttpEntity(
        ContentTypes.`application/json`,
        """{
          |    "deviceId": "10036",
          |    "email": "email"
          |}""".stripMargin),
      headers = List(adminAuth),
      protocol = `HTTP/1.0`) ~> route ~> check {
      status shouldBe StatusCodes.Unauthorized
    }


    HttpRequest(
      POST,
      uri = "/admin/create-admin-user",
      entity = HttpEntity(
        ContentTypes.`application/json`,
        """{
          |    "email": "email",
          |    "password": "password",
          |    "role":"admin"
          |}""".stripMargin),
      headers = List(adminAuth),
      protocol = `HTTP/1.0`) ~> route ~> check {
      status shouldBe StatusCodes.Unauthorized
    }
  }


  "return Unauthorized if user sends request" in {
    when(adminDAO.getResourcePermissions) thenReturn Future((rolesResourceAccessDB, resourcePermissionsDB))
    Get("/admin/get-user-details?deviceId=100").addHeader(userAuth) ~> route ~> check {
      status shouldEqual StatusCodes.Unauthorized
    }

    HttpRequest(
      POST,
      uri = "/admin/create-admin-user",
      entity = HttpEntity(
        ContentTypes.`application/json`,
        """{
          |    "email": "email",
          |    "password": "password",
          |    "role":"admin"
          |}""".stripMargin),
      headers = List(userAuth),
      protocol = `HTTP/1.0`) ~> route ~> check {
      status shouldBe StatusCodes.Unauthorized
    }

    HttpRequest(
      POST,
      uri = "/admin/create-user",
      entity = HttpEntity(
        ContentTypes.`application/json`,
        """{
          |    "deviceId": "10003",
          |    "phone": "9684356",
          |    "email": "email",
          |    "isDisabled":false
          |}""".stripMargin),
      headers = List(userAuth),
      protocol = `HTTP/1.0`) ~> route ~> check {
      status shouldBe StatusCodes.Unauthorized
    }

    HttpRequest(
      POST,
      uri = "/admin/update-user",
      entity = HttpEntity(
        ContentTypes.`application/json`,
        """{
          |    "deviceId": "10003",
          |    "phone": "9684356",
          |    "email": "email",
          |    "isDisabled":false
          |}""".stripMargin),
      headers = List(userAuth),
      protocol = `HTTP/1.0`) ~> route ~> check {
      status shouldBe StatusCodes.Unauthorized
    }

    HttpRequest(
      POST,
      uri = "/admin/disable-user",
      entity = HttpEntity(
        ContentTypes.`application/json`,
        """{
          |    "deviceId": "10036",
          |    "email": "email",
          |    "isDisabled":true
          |}""".stripMargin),
      headers = List(userAuth),
      protocol = `HTTP/1.0`) ~> route ~> check {
      status shouldBe StatusCodes.Unauthorized
    }

    HttpRequest(
      DELETE,
      uri = "/admin/delete-user",
      entity = HttpEntity(
        ContentTypes.`application/json`,
        """{
          |    "deviceId": "10036",
          |    "email": "email"
          |}""".stripMargin),
      headers = List(userAuth),
      protocol = `HTTP/1.0`) ~> route ~> check {
      status shouldBe StatusCodes.Unauthorized
    }
  }


  override implicit val routeCache: RoleAuthorizationCache = new RoleAuthorizationCache(adminDAO)

}
