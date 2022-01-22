package ca.pigscanfly.components


import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape

case class ResourcePermissionsDB(resource: String, permission: String)

case class ResourcePermissions(resource: String, permission: List[String])

case class RolesResourceAccessDB(userType: String,
                                 resource: String,
                                 id: Int)

case class RolesResourceAccess(userType: String,
                               resource: List[String])

final class ResourcePermissionsTable(tag: Tag)(implicit val schema: String)
  extends Table[ResourcePermissionsDB](tag,
    Some(schema),
    "resource_permission") {

  //noinspection ScalaStyle
  def * : ProvenShape[ResourcePermissionsDB] =
    (
      resource,
      permission
      ).shaped <> (ResourcePermissionsDB.tupled, ResourcePermissionsDB.unapply)

  def resource: Rep[String] = column[String]("resource")

  def permission: Rep[String] = column[String]("permission")
}

final class RolesResourceAccessTable(tag: Tag)(implicit val schema: String)
  extends Table[RolesResourceAccessDB](tag,
    Some(schema),
    "roles_resource_access") {

  //noinspection ScalaStyle
  def * : ProvenShape[RolesResourceAccessDB] =
    (
      userType,
      resource,
      id
      ).shaped <> (RolesResourceAccessDB.tupled, RolesResourceAccessDB.unapply)

  def userType: Rep[String] = column[String]("user_type")

  def resource: Rep[String] = column[String]("resource")

  def id: Rep[Int] = column[Int]("id")
}
