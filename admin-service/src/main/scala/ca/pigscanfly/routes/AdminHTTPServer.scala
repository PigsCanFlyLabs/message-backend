package ca.pigscanfly.routes

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.routing.RoundRobinPool
import akka.stream.ActorMaterializer
import ca.pigscanfly.actor.AdminActor
import ca.pigscanfly.cache.RoleAuthorizationCache
import ca.pigscanfly.configs.Constants._
import ca.pigscanfly.dao._
import ca.pigscanfly.db.DatabaseApi.api._
import ca.pigscanfly.flyway.FlywayService
import ca.pigscanfly.models.DBConfig
import org.fusesource.jansi.Ansi.Color._
import org.fusesource.jansi.Ansi._

import scala.concurrent.duration._
import scala.util.{Failure, Success}

object AdminHTTPServer
  extends App
    with AdminService {

  implicit val system: ActorSystem = ActorSystem("admin-service")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  import system.dispatcher

  lazy val routes: Route = getUserRoutes(adminServiceActor)

  val dbConfig = DBConfig(profile = dbProfile,
    driver = dbDriver,
    url = dbUrl,
    user = dbUser,
    password = dbPassword,
    schema = dbSchema,
    threadsPoolCount = dbThreadsPoolCount,
    queueSize = dbQueueSize,
    searchLimit = dbSearchLimit)

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

  val swarmDAO = new SwarmDAO()

  implicit val routeCache: RoleAuthorizationCache = new RoleAuthorizationCache(swarmDAO)
  val adminServiceActor: ActorRef = system.actorOf(
    AdminActor
      .props(swarmDAO)
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
      //TODO: replace with logger
      println(
        s"Server is listening on ${localAddress.getHostName}:${localAddress.getPort}")
    case Failure(e) ⇒
      println(s"Binding failed with ${e.getMessage}")
      system.terminate()
  }
}
