package ca.pigscanfly.dao

import ca.pigscanfly.components._
import ca.pigscanfly.connections._
import com.typesafe.scalalogging.LazyLogging
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class AdminDAO(implicit val db: Database,
               schema: String,
               ec: ExecutionContext)
  extends LazyLogging {

  val rolesResourcesQuery = TableQuery[RolesResourceAccessTable]
  val resourcePermissionsQuery = TableQuery[ResourcePermissionsTable]
  val adminLoginQuery = TableQuery[AdminLoginMapping]

  /**
   * Get roles and permissions to generate cache for roles w.r.t to route
   *
   * @return It return two sequences:
   *         one contains permissions with respect to  list of routes
   *         another sequence contains role with respect to list of permission
   */
  def getResourcePermissions
  : Future[(Seq[RolesResourceAccessDB], Seq[ResourcePermissionsDB])] = {
    logger.info("AdminDAO: Fetching roles_resource_access table and resource_permission table to generate cache for roles and route permissions.")
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
   * Method to validate admin user with email and role for creating admin user
   * Returns Future(0) for if user does not exists and Future(1) if user exists
   *
   * @param email : email of admin/ super admin
   * @param role  : role of admin/ super admin
   * @return Future[Int]
   */
  def checkIfAdminExists(email: String, role: String): Future[Int] = {
    logger.info(s"AdminDAO: Checking if details exists for email: $email and role: $role exists in admin_login table.")
    val query = adminLoginQuery
      .filter(col => col.email === email &&
        col.role === role)
      .size
      .result
    db.run(query)
  }

  /**
   * Validate admin login using email, password and role to get him logged in the system
   * Returns Future(0) for failure and Future(1) for successful response
   *
   * @param adminLogin : contains email, password and role
   * @return Future[Int]
   */
  def validateAdminLogin(adminLogin: AdminLogin): Future[Int] = {
    logger.info(s"AdminDAO: Checking if details exists for email: ${adminLogin.email}, password: ***** and role: ${adminLogin.role} exists in admin_login table.")
    val query = adminLoginQuery
      .filter(col => col.email === adminLogin.email &&
        col.password === adminLogin.password &&
        col.role === adminLogin.role)
      .size
      .result
    db.run(query)
  }

  /**
   * Create admin user with email, password and roles
   * Returns Future(0) for failure and Future(1) for successful insertion
   *
   * @param details : contains (email, password, role)
   * @return Future[Int]
   */
  def createAdminUser(details: AdminLogin): Future[Int] = {
    logger.info(s"AdminDAO: Inserting  email: ${details.email}, password: ***** and role: ${details.role} in admin_login table.")
    db.run(adminLoginQuery += details)
  }

}
