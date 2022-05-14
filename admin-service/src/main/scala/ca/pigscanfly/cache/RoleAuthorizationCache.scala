package ca.pigscanfly.cache

import ca.pigscanfly.components.{ResourcePermissions, ResourcePermissionsDB, RolesResourceAccess, RolesResourceAccessDB}
import ca.pigscanfly.dao.AdminDAO
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration.{FiniteDuration, _}
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

case class Permissions(permission: List[String])

class RoleAuthorizationCache(accountsDAO: AdminDAO) extends CacheWithFallbackToStaleData[String, Map[String, Permissions]] with LazyLogging {

  /**
   * Indicates how to fetch the value for a given key.
   * This function is called when the key is not cached or the value has expired.
   *
   * If the function throws an exception and there is a value in the cache, this stale value will be returned.
   * Otherwise the exception is thrown and must be handled.
   *
   * @see [[safeGet]] and [[getOrElse]]
   */
  override def duration: FiniteDuration = FiniteDuration(30, DAYS)

  override def refresh(key: String): Map[String, Permissions] = {
    val resourcePermissions: Future[(Seq[RolesResourceAccessDB], Seq[ResourcePermissionsDB])] =
      accountsDAO
        .getResourcePermissions
    val resp = Await.result(resourcePermissions, 5 second)
    val roles: Seq[RolesResourceAccess] = resp._1.map(roleAccess =>
      RolesResourceAccess(roleAccess.userType,
        roleAccess.resource.split(",").toList)) //RolesResourceAccess(userType: String, resource: List[String])
    val resources: Seq[ResourcePermissions] = resp._2.map(rolePermission =>
      ResourcePermissions(rolePermission.resource,
        rolePermission.permission.split(",").toList)
    ) //ResourcePermissions(resource: String, permission: List[String])
    val result: Seq[String] = roles.filter(_.userType == key).flatMap { rol =>
      rol.resource.flatMap { rest =>
        val routes = resources.filter(_.resource == rest)
        routes.flatMap(_.permission)

      }
    }
    Map(key -> Permissions(result.toList))
  }

  def findRoleAndRouteAccess(role: String, route: String): Boolean = {
    val permissions: Option[Permissions] = findByUserRole(role).get(role)
    val result = checkRoleAuth(permissions, route)
    logger.info(s"RoleAuthorizationCache: $role isValidRole: $result")
    result
  }

  def findByUserRole(userRole: String): Map[String, Permissions] = {
    get(userRole)
  }

  def checkRoleAuth(permissions: Option[Permissions],
                    resource: String): Boolean = {
    permissions match {
      case Some(permissions) => permissions.permission.contains(resource)
      case None => false
    }
  }
}
