package ca.pigscanfly.components


import ca.pigscanfly.db.DatabaseApi.api._
import slick.lifted.ProvenShape

case class RouteResources(route: String, resource: String)

case class ResourcePermissions(resource: String, permission: List[String])

case class RolesResourceAccess(userType: String,
                               resource: List[String],
                               id: Int)

final class RouteResourcesTable(tag: Tag)(implicit val schema: String)
  extends Table[RouteResources](tag, Some(schema), "route_resource") {

  //noinspection ScalaStyle
  def * : ProvenShape[RouteResources] =
    (
      route,
      resource
      ).shaped <> (RouteResources.tupled, RouteResources.unapply)

  def route: Rep[String] = column[String]("route")

  def resource: Rep[String] = column[String]("resource")
}

final class ResourcePermissionsTable(tag: Tag)(implicit val schema: String)
  extends Table[ResourcePermissions](tag,
    Some(schema),
    "resource_permission") {

  //noinspection ScalaStyle
  def * : ProvenShape[ResourcePermissions] =
    (
      resource,
      permission
      ).shaped <> (ResourcePermissions.tupled, ResourcePermissions.unapply)

  def resource: Rep[String] = column[String]("resource")

  def permission: Rep[List[String]] = column[List[String]]("permission")
}

final class RolesResourceAccessTable(tag: Tag)(implicit val schema: String)
  extends Table[RolesResourceAccess](tag,
    Some(schema),
    "roles_resource_access") {

  //noinspection ScalaStyle
  def * : ProvenShape[RolesResourceAccess] =
    (
      userType,
      resource,
      id
      ).shaped <> (RolesResourceAccess.tupled, RolesResourceAccess.unapply)

  def userType: Rep[String] = column[String]("user_type")

  def resource: Rep[List[String]] = column[List[String]]("resource")

  def id: Rep[Int] = column[Int]("id")
}
