package ca.pigscanfly.routes

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.routing.RoundRobinPool
import akka.stream.ActorMaterializer
import ca.pigscanfly.actor.AdminActor
import ca.pigscanfly.cache.RoleAuthorizationCache
import ca.pigscanfly.configs.AdminConstants._
import ca.pigscanfly.dao._
import ca.pigscanfly.flyway.FlywayService
import com.typesafe.scalalogging.LazyLogging
import org.fusesource.jansi.Ansi.Color._
import org.fusesource.jansi.Ansi._
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.duration._
import scala.util.{Failure, Success}

object AdminHTTPServer
  extends App
    with AdminService
    with LazyLogging {

  implicit val system: ActorSystem = ActorSystem("admin-service")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  import system.dispatcher

  lazy val routes: Route = adminUserRoutes(adminServiceActor)

  implicit val db: Database = Database.forURL(
    url = dbConfig.url,
    user = dbConfig.user,
    password = dbConfig.password,
    driver = dbConfig.driver,
    executor = AsyncExecutor("postgres",
      numThreads = dbConfig.threadsPoolCount,
      queueSize = dbConfig.queueSize)
  )
  implicit val schema: String = dbConfig.schema

  implicit val searchLimit: Int = dbConfig.searchLimit

  val futureAwaitTime: FiniteDuration = akkaAwaitDuration.minutes

  implicit val futureAwaitDuration: FiniteDuration =
    FiniteDuration(futureAwaitTime.length, futureAwaitTime.unit)

  val flyWayService = new FlywayService(dbConfig)
  flyWayService.migrateDatabaseSchema()

  val adminDAO = new AdminDAO()
  val userDAO = new UserDAO()

  implicit val routeCache: RoleAuthorizationCache = new RoleAuthorizationCache(adminDAO)
  val adminServiceActor: ActorRef = system.actorOf(
    AdminActor
      .props(adminDAO, userDAO)
      .withRouter(RoundRobinPool(nrOfInstances = 10)),
    "admin-service"
  )
  //bind route to server
  val binding = Http().bindAndHandle(routes, adminHost, adminPort)

  //scalastyle:off
  binding.onComplete {
    case Success(binding) ⇒
      val localAddress = binding.localAddress
      println(
        ansi()
          .fg(GREEN)
          .a(
            """
              |╔═══╗╔═══╗╔═╗╔═╗╔══╗╔═╗ ╔╗    ╔═══╗╔═══╗╔═══╗╔╗  ╔╗╔══╗╔═══╗╔═══╗
              |║╔═╗║╚╗╔╗║║║╚╝║║╚╣╠╝║║╚╗║║    ║╔═╗║║╔══╝║╔═╗║║╚╗╔╝║╚╣╠╝║╔═╗║║╔══╝
              |║║ ║║ ║║║║║╔╗╔╗║ ║║ ║╔╗╚╝║    ║╚══╗║╚══╗║╚═╝║╚╗║║╔╝ ║║ ║║ ╚╝║╚══╗
              |║╚═╝║ ║║║║║║║║║║ ║║ ║║╚╗║║    ╚══╗║║╔══╝║╔╗╔╝ ║╚╝║  ║║ ║║ ╔╗║╔══╝
              |║╔═╗║╔╝╚╝║║║║║║║╔╣╠╗║║ ║║║    ║╚═╝║║╚══╗║║║╚╗ ╚╗╔╝ ╔╣╠╗║╚═╝║║╚══╗
              |╚╝ ╚╝╚═══╝╚╝╚╝╚╝╚══╝╚╝ ╚═╝    ╚═══╝╚═══╝╚╝╚═╝  ╚╝  ╚══╝╚═══╝╚═══╝
              |""".stripMargin)
          .reset())
      //scalastyle:on
      logger.info(
        s"Server is listening on ${localAddress.getHostName}:${localAddress.getPort}")
    case Failure(e) ⇒
      logger.info(s"Binding failed with ${e.getMessage}")
      system.terminate()
  }
}
