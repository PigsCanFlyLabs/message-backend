package ca.pigscanfly.cache

import ca.pigscanfly.components.{ResourcePermissions, RolesResourceAccess}
import ca.pigscanfly.dao.SwarmDAO

import scala.concurrent.duration.{FiniteDuration, _}
import scala.concurrent.{Await, Future}

case class Permissions(permission: List[String])

class RoleAuthorizationCache(accountsDAO: SwarmDAO)
  extends CacheWithFallbackToStaleData[String, Map[String, Permissions]] {

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
    val f: Future[(Seq[RolesResourceAccess], Seq[ResourcePermissions])] =
      accountsDAO
        .getResourcePermissions()
    val resp = Await.result(f, 5 second)
    val roles = resp._1 //RolesResourceAccess(userType: String, resource: List[String])
    val resources = resp._2 //ResourcePermissions(resource: String, permission: List[String])
    val result = roles.filter(_.userType == key).flatMap { rol =>
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
    //TODO: replace with logger
    println(s"isValidRole $role : $result")
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
