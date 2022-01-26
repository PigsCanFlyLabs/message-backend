package ca.pigscanfly.routes

import akka.actor.ActorRef
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ca.pigscanfly.cache.RoleAuthorizationCache
import ca.pigscanfly.components._
import ca.pigscanfly.handler.AdminHandler
import ca.pigscanfly.models.JWTTokenHelper
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

/**
 * Service to handle the account of the user
 */
trait AdminService
  extends AdminHandler
    with JWTTokenHelper {
  // ==============================
  //     REST ROUTES
  // ==============================

  implicit val routeCache: RoleAuthorizationCache
  val unauthorizedRouteResponse = HttpResponse(status = StatusCodes.Unauthorized,
    headers = Nil,
    entity = HttpEntity.Empty,
    protocol = HttpProtocols.`HTTP/1.1`)

  def adminUserRoutes(actor: ActorRef): Route =
    pathPrefix("admin") {
      path("get-user-details") {
        parameters('deviceId) {
          (deviceId: String) =>
            authenticateOAuth2("Bearer Authentication", roleAuthenticator) { auth =>
              val isAccessible =
                routeCache.findRoleAndRouteAccess(auth.role, "get-user-details")
              if (isAccessible) {
                val response =
                  getUserDetails(actor, deviceId.toLong)
                complete(response)
              } else {
                complete(unauthorizedRouteResponse)
              }
            }
        }
      } ~
        path("create-admin-user") {
          pathEnd {
            authenticateOAuth2("Bearer Authentication", roleAuthenticator) { auth =>
              val isAccessible =
                routeCache.findRoleAndRouteAccess(auth.role, "create-admin-user")
              if (isAccessible) {
                (post & entity(as[AdminLogin])) { request =>
                  val response =
                    createAdmin(actor, request)
                  complete(response)
                }
              } else {
                complete(unauthorizedRouteResponse)
              }
            }
          }
        } ~ path("create-user") {
        pathEnd {
          authenticateOAuth2("Bearer Authentication", roleAuthenticator) { auth =>
            val isAccessible =
              routeCache.findRoleAndRouteAccess(auth.role, "create-user")
            if (isAccessible) {
              (post & entity(as[User])) { request =>
                val response =
                  createUser(actor, request)
                complete(response)
              }
            } else {
              complete(unauthorizedRouteResponse)
            }
          }
        }
      } ~ path("update-user") {
        pathEnd {
          authenticateOAuth2("Bearer Authentication", roleAuthenticator) { auth =>
            val isAccessible =
              routeCache.findRoleAndRouteAccess(auth.role, "update-user")
            if (isAccessible) {
              (post & entity(as[User])) { request =>
                val response =
                  updateUser(actor, request)
                complete(response)
              }
            } else {
              complete(unauthorizedRouteResponse)
            }
          }
        }
      } ~ path("disable-user") {
        pathEnd {
          authenticateOAuth2("Bearer Authentication", roleAuthenticator) { auth =>
            val isAccessible =
              routeCache.findRoleAndRouteAccess(auth.role, "disable-user")
            if (isAccessible) {
              (post & entity(as[DisableUserRequest])) { request =>
                val response =
                  disableUser(actor, request)
                complete(response)
              }
            } else {
              complete(unauthorizedRouteResponse)
            }
          }
        }
      } ~ path("delete-user") {
        pathEnd {
          authenticateOAuth2("Bearer Authentication", roleAuthenticator) { auth =>
            val isAccessible =
              routeCache.findRoleAndRouteAccess(auth.role, "delete-user")
            if (isAccessible) {
              (delete & entity(as[DeleteUserRequest])) { request =>
                val response =
                  deleteUser(actor, request)
                complete(response)
              }
            } else {
              complete(unauthorizedRouteResponse)
            }
          }
        }
      } ~ path("login") {
        (post & entity(as[AdminLogin])) { request =>
          val response =
            adminLogin(actor, request)
          complete(response)
        }
      }
    }
}
