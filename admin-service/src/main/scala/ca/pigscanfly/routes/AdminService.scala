package ca.pigscanfly.routes

import akka.actor.ActorRef
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ca.pigscanfly.cache.RoleAuthorizationCache
import ca.pigscanfly.components._
import ca.pigscanfly.handler.AdminHandler
import ca.pigscanfly.models.JWTTokenHelper
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

/**
 * Service to handle the admin routes
 */
trait AdminService
  extends AdminHandler
    with JWTTokenHelper
    with LazyLogging {
  // ==============================
  //     REST ROUTES
  // ==============================

  implicit val routeCache: RoleAuthorizationCache
  val unauthorizedRouteResponse: HttpResponse = HttpResponse(status = StatusCodes.Unauthorized,
    headers = Nil,
    entity = HttpEntity.Empty,
    protocol = HttpProtocols.`HTTP/1.1`)

  def adminUserRoutes(actor: ActorRef): Route =
    pathPrefix("admin") {
      path("get-user-details") { // Route that takes query parameter(user's device_id) in request to fetch user details
        parameters('deviceId) {
          (deviceId: String) =>
            authenticateOAuth2("Bearer Authentication", roleAuthenticator) { auth =>
              val isAccessible =
                routeCache.findRoleAndRouteAccess(auth.role, "get-user-details")
              if (isAccessible) {
                logger.info(s"AdminService: Received request to get user details for device_id: $deviceId")
                val response =
                  getUserDetails(actor, deviceId.toLong)
                complete(response)
              } else {
                complete(unauthorizedRouteResponse)
              }
            }
        }
      } ~
        path("create-admin-user") { //Route that takes raw json request to create admin or super admin with the requested email, role and password
          pathEnd {
            authenticateOAuth2("Bearer Authentication", roleAuthenticator) { auth =>
              val isAccessible =
                routeCache.findRoleAndRouteAccess(auth.role, "create-admin-user")
              if (isAccessible) {
                (post & entity(as[AdminLogin])) { request =>
                  logger.info(s"AdminService: Received request to create admin OR super admin email: ${request.email}, role: ${request.role}")
                  val response =
                    createAdmin(actor, request)
                  complete(response)
                }
              } else {
                complete(unauthorizedRouteResponse)
              }
            }
          }
        } ~
        path("create-user") { //Route that takes raw json request to create user with the requested device_id, email, phone_number, is_disabled(identifier to check if user has subscription or not)
          pathEnd {
            authenticateOAuth2("Bearer Authentication", roleAuthenticator) { auth =>
              val isAccessible =
                routeCache.findRoleAndRouteAccess(auth.role, "create-user")
              if (isAccessible) {
                (post & entity(as[User])) { request =>
                  logger.info(s"AdminService: Received request to create new user device_id: ${request.deviceId}, email: ${request.email}, phone_number: ${request.phone}")
                  val response =
                    createUser(actor, request)
                  complete(response)
                }
              } else {
                complete(unauthorizedRouteResponse)
              }
            }
          }
        } ~
        path("update-user") { //Route that takes raw json request to update user's email and phone_number on the basis of device_id
          pathEnd {
            authenticateOAuth2("Bearer Authentication", roleAuthenticator) { auth =>
              val isAccessible =
                routeCache.findRoleAndRouteAccess(auth.role, "update-user")
              if (isAccessible) {
                (post & entity(as[UpdateUserRequest])) { request =>
                  logger.info(s"AdminService: Received request to update user details request: $request")
                  val response =
                    updateUser(actor, request)
                  complete(response)
                }
              } else {
                complete(unauthorizedRouteResponse)
              }
            }
          }
        } ~
        path("disable-user") { //Route that takes raw json request to disable user on the basis of device_id
          pathEnd {
            authenticateOAuth2("Bearer Authentication", roleAuthenticator) { auth =>
              val isAccessible =
                routeCache.findRoleAndRouteAccess(auth.role, "disable-user")
              if (isAccessible) {
                (post & entity(as[DisableUserRequest])) { request =>
                  logger.info(s"AdminService: Received request to disable user request: $request")
                  val response =
                    disableUser(actor, request)
                  complete(response)
                }
              } else {
                complete(unauthorizedRouteResponse)
              }
            }
          }
        } ~
        path("delete-user") { //Route that takes raw json request to delete user on the basis of device_id
          pathEnd {
            authenticateOAuth2("Bearer Authentication", roleAuthenticator) { auth =>
              val isAccessible =
                routeCache.findRoleAndRouteAccess(auth.role, "delete-user")
              if (isAccessible) {
                (delete & entity(as[DeleteUserRequest])) { request =>
                  logger.info(s"AdminService: Received request to delete user request: $request")
                  val response =
                    deleteUser(actor, request)
                  complete(response)
                }
              } else {
                complete(unauthorizedRouteResponse)
              }
            }
          }
        } ~
        path("login") { //Route that takes raw json request to validate admin OR super admin for login with the requested email, password and role
          (post & entity(as[AdminLogin])) { request =>
            logger.info(s"AdminService: Received request for login in the admin panel email: ${request.email}, role: ${request.role}")
            val response =
              adminLogin(actor, request)
            complete(response)
          }
        }
    }
}
