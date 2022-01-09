package ca.pigscanfly.dao

import ca.pigscanfly.components._
import ca.pigscanfly.connections._
import ca.pigscanfly.db.DatabaseApi.api._
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class SwarmDAO(implicit val db: Database,
               schema: String,
               ec: ExecutionContext) {

  val rolesResourcesQuery = TableQuery[RolesResourceAccessTable]
  val resourcePermissionsQuery = TableQuery[ResourcePermissionsTable]
  val userQuery = TableQuery[UsersMapping]
  val adminLoginQuery = TableQuery[AdminLoginMapping]

  def getResourcePermissions()
  : Future[(Seq[RolesResourceAccess], Seq[ResourcePermissions])] = {
    val roles = rolesResourcesQuery.result
    val resources = resourcePermissionsQuery.result
    val result = db.run(
      for {
        role <- roles
        resource <- resources
      } yield (role, resource)
    )
    result
  }

  def checkIfAdminExists(email: String, role: String): Future[Int] = {
    val query = adminLoginQuery
      .filter(col => (col.email === email &&
        col.role === role))
      .size
      .result
    db.run(query)
  }

  def checkIfUserExists(email: String, deviceId: String): Future[Int] = {
    val query = userQuery
      .filter(col => (col.email === email &&
        col.deviceId === deviceId))
      .size
      .result
    db.run(query)
  }

  def getUserDetails(email: String, deviceId: String): Future[Option[User]] = {
    val query = userQuery
      .filter(col => (col.email === email &&
        col.deviceId === deviceId))
      .result
      .headOption
    db.run(query)
  }

  def insertUserDetails(user: User): Future[Int] = db.run(userQuery += user)

  def updateUserDetails(user: User): Future[Int] = {
    val query = userQuery
      .filter(col => (col.email === user.email &&
        col.deviceId === user.deviceId))
      .map(col => (col.name, col.isDisabled))
      .update((user.name, user.isDisabled))
    db.run(query)
  }

  def disableUser(request: DisableUserRequest): Future[Int] = {
    val query = userQuery
      .filter(col => (col.email === request.email &&
        col.deviceId === request.deviceId))
      .map(_.isDisabled)
      .update(request.isDisabled)
    db.run(query)
  }

  def deleteUser(request: DeleteUserRequest): Future[Int] = {
    val query = userQuery
      .filter(col => (col.email === request.email &&
        col.deviceId === request.deviceId))
      .delete
    db.run(query)
  }

  def validateAdminLogin(adminLogin: AdminLogin): Future[Int] = {
    val query = adminLoginQuery
      .filter(col => (col.email === adminLogin.email &&
        col.password === adminLogin.password &&
        col.role === adminLogin.role))
      .size
      .result
    db.run(query)
  }

  def createAdminUser(details: AdminLogin): Future[Int] = db.run(adminLoginQuery += details)

}
