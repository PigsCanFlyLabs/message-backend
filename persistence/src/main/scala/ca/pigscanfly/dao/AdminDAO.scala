package ca.pigscanfly.dao

import ca.pigscanfly.components._
import ca.pigscanfly.connections._
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class AdminDAO(implicit val db: Database,
               schema: String,
               ec: ExecutionContext) {

  val rolesResourcesQuery = TableQuery[RolesResourceAccessTable]
  val resourcePermissionsQuery = TableQuery[ResourcePermissionsTable]
  val adminLoginQuery = TableQuery[AdminLoginMapping]

  /**
   * Get roles and permissions
   *
   * @return Future[(Seq[RolesResourceAccessDB], Seq[ResourcePermissionsDB])]
   */
  def getResourcePermissions
  : Future[(Seq[RolesResourceAccessDB], Seq[ResourcePermissionsDB])] = {
    val roles = rolesResourcesQuery.result
    val resources = resourcePermissionsQuery.result
    db.run(
      for {
        role <- roles
        resource <- resources
      } yield (role, resource)
    )
  }

  /**
   * Method to validate admin user
   *
   * @param email
   * @param role
   * @return Future[Int]
   */
  def checkIfAdminExists(email: String, role: String): Future[Int] = {
    val query = adminLoginQuery
      .filter(col => col.email === email &&
        col.role === role)
      .size
      .result
    db.run(query)
  }

  /**
   * Validate admin login
   *
   * @param adminLogin
   * @return Future[Int]
   */
  def validateAdminLogin(adminLogin: AdminLogin): Future[Int] = {
    val query = adminLoginQuery
      .filter(col => col.email === adminLogin.email &&
        col.password === adminLogin.password &&
        col.role === adminLogin.role)
      .size
      .result
    db.run(query)
  }

  /**
   * Create admin user
   *
   * @param details
   * @return Future[Int]
   */
  def createAdminUser(details: AdminLogin): Future[Int] = db.run(adminLoginQuery += details)

}
