package ca.pigscanfly.dao

import ca.pigscanfly.components._
import ca.pigscanfly.connections._
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class UserDAO(implicit val db: Database,
              ec: ExecutionContext) {

  val userQuery = TableQuery[UsersMapping]

  /**
   * Check if user exists
   * @param email
   * @param deviceId
   * @return Future[Int]
   */
  def checkIfUserExists(email: Option[String], deviceId: Long): Future[Int] = {
    val query = userQuery
      .filter(col => col.email === email &&
        col.deviceId === deviceId)
      .size
      .result
    db.run(query)
  }

  /**
   * Retrieves user details on the basis of deviceId
   * @param deviceId
   * @return Future[Option[User]]
   */
  def getUserDetails(deviceId: Long): Future[Option[User]] = {
    val query = userQuery
      .filter(col => col.deviceId === deviceId)
      .result
      .headOption
    db.run(query)
  }

  /**
   * Create user
   * @param user: User
   * @return Future[Int]
   */
  def insertUserDetails(user: User): Future[Int] = db.run(userQuery += user)

  /**
   * Update user details
   * @param user: UpdateUserRequest
   * @return Future[Int]
   */
  def updateUserDetails(user: UpdateUserRequest): Future[Int] = {
    val query = userQuery
      .filter(_.deviceId === user.deviceId)
      .map(col => (col.phone, col.email))
      .update((user.phone, user.email))
    db.run(query)
  }

  /**
   * Disable user
   * @param request: DisableUserRequest
   * @return Future[Int]
   */
  def disableUser(request: DisableUserRequest): Future[Int] = {
    val query = userQuery
      .filter(_.deviceId === request.deviceId)
      .map(_.isDisabled)
      .update(request.isDisabled)
    db.run(query)
  }

  /**
   * Check if user is subscribed
   * @param deviceId
   * @return Future[Option[Boolean]]
   */
  def checkUserSubscription(deviceId: Long): Future[Option[Boolean]] = {
    val query = userQuery
      .filter(col => col.deviceId === deviceId)
      .map(_.isDisabled)
      .result
      .headOption
    db.run(query)
  }

  /**
   * Delete user
   * @param request: DeleteUserRequest
   * @return Future[Int]
   */
  def deleteUser(request: DeleteUserRequest): Future[Int] = {
    val query = userQuery
      .filter(_.deviceId === request.deviceId)
      .delete
    db.run(query)
  }

  /**
   * Retrieves device Id from email or phone
   * @param from
   * @return Future[Option[Long]]
   */
  def getDeviceIdFromEmailOrPhone(from: String): Future[Option[Long]] = {
    val query = userQuery
      .filter(col => col.email === from || col.phone === from)
      .map(_.deviceId)
      .result
      .headOption
    db.run(query)
  }

  /**
   * Retrieves email or phone on the basis of device id
   * @param deviceId
   * @return Future[Option[(Option[String], Option[String])]]
   */
  def getEmailOrPhoneFromDeviceId(deviceId: Long): Future[Option[(Option[String], Option[String])]] = {
    val query = userQuery
      .filter(col => col.deviceId === deviceId)
      .map(col => (col.phone, col.email))
      .result
      .headOption
    db.run(query)
  }

}
