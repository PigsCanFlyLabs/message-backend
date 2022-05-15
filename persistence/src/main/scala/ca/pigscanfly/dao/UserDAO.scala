package ca.pigscanfly.dao

import ca.pigscanfly.components._
import ca.pigscanfly.connections._
import com.typesafe.scalalogging.LazyLogging
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class UserDAO(implicit val db: Database,
              ec: ExecutionContext)
  extends LazyLogging {

  val userQuery = TableQuery[UsersMapping]

  /**
   * Check if user exists for new user creation
   * Returns Future(0) if user doesn't exist and Future(1) if user exists
   *
   * @param email    : user email
   * @param deviceId : user's device Id
   * @return Future[Int]
   */
  def checkIfUserExists(email: Option[String], deviceId: Long): Future[Int] = {
    logger.info(s"UserDAO: Checking if details exists for email: ${email.getOrElse("")} and deviceId: $deviceId exists in spacebeaver.users_mapping table.")
    val query = userQuery
      .filter(col => col.email === email &&
        col.deviceId === deviceId)
      .size
      .result
    db.run(query)
  }

  /**
   * Retrieves user details on the basis of deviceId
   * Returns Future(None) if user doesn't exist and Future(Some(User(customerId: Option[String],deviceId: Long,phone: Option[String],email: Option[String],isDisabled: Boolean))) if user details are found
   *
   * @param deviceId : user device Id
   * @return Future[Option[User]]
   */
  def getUserDetails(deviceId: Long): Future[Option[User]] = {
    logger.info(s"UserDAO: Fetching details exists for deviceId: $deviceId exists from spacebeaver.users_mapping table.")
    val query = userQuery
      .filter(col => col.deviceId === deviceId)
      .result
      .headOption
    db.run(query)
  }

  /**
   * Create new user
   * Returns Future(0) for failure and Future(1) for successful insertion
   *
   * @param user : User(customerId: Option[String],deviceId: Long,phone: Option[String],email: Option[String],isDisabled: Boolean)
   * @return Future[Int]
   */
  def insertUserDetails(user: User): Future[Int] = {
    logger.info(s"UserDAO: Inserting user details i.e. customerId: ${user.customerId}, deviceId: ${user.deviceId},phone: ${user.phone},email: ${user.email},isDisabled: ${user.isDisabled} in spacebeaver.users_mapping table.")
    db.run(userQuery += user)
  }

  /**
   * Update user details
   * Returns Future(0) for failure and Future(1) for successful updation
   *
   * @param user : UpdateUserRequest(deviceId: Long, phone: Option[String], email: Option[String])
   * @return Future[Int]
   */
  def updateUserDetails(user: UpdateUserRequest): Future[Int] = {
    logger.info(s"UserDAO: Updating user details i.e. phone: ${user.phone} and email: ${user.email} for device_id:${user.deviceId} in spacebeaver.users_mapping table.")
    val query = userQuery
      .filter(_.deviceId === user.deviceId)
      .map(col => (col.phone, col.email))
      .update((user.phone, user.email))
    db.run(query)
  }

  /**
   * Disable user
   * Returns Future(0) for failure and Future(1) if succeed to disable user
   *
   * @param request : DisableUserRequest(deviceId: Long, isDisabled: Boolean)
   * @return Future[Int]
   */
  def disableUser(request: DisableUserRequest): Future[Int] = {
    logger.info(s"UserDAO: Updating user details i.e. is_disabled: ${request.isDisabled} for device_id:${request.deviceId} in spacebeaver.users_mapping table.")
    val query = userQuery
      .filter(_.deviceId === request.deviceId)
      .map(_.isDisabled)
      .update(request.isDisabled)
    db.run(query)
  }

  /**
   * Check if user has the subscription for his device Id in our system
   * Returns Future(None) if user does not exists and if user exists it returns Future(Some(is_disabled))
   *
   * @param deviceId : users device Id
   * @return Future[Option[Boolean]]
   */
  def checkUserSubscription(deviceId: Long): Future[Option[Boolean]] = {
    logger.info(s"UserDAO: Fetching user's is_disabled for device_id:$deviceId from spacebeaver.users_mapping table.")
    val query = userQuery
      .filter(col => col.deviceId === deviceId)
      .map(_.isDisabled)
      .result
      .headOption
    db.run(query)
  }

  /**
   * Delete user from the system
   * Returns Future(0) if user doesn't exist and Future(1) if succeed to delete user
   *
   * @param request : DeleteUserRequest(deviceId: Long)
   * @return Future[Int]
   */
  def deleteUser(request: DeleteUserRequest): Future[Int] = {
    logger.info(s"UserDAO: Deleting user device_id:${request.deviceId} from spacebeaver.users_mapping table.")
    val query = userQuery
      .filter(_.deviceId === request.deviceId)
      .delete
    db.run(query)
  }

  /**
   * Retrieves device Id from email or phone
   * Returns Future(None) if user does not exists and if user exists it returns Future(Some(device_id))
   *
   * @param from : user's email or phone number
   * @return Future[Option[Long]]
   */
  def getDeviceIdFromEmailOrPhone(from: String): Future[Option[Long]] = {
    logger.info(s"UserDAO: Fetching user's device_id for email/phone_number: $from from spacebeaver.users_mapping table.")
    val query = userQuery
      .filter(col => col.email === from || col.phone === from)
      .map(_.deviceId)
      .result
      .headOption
    db.run(query)
  }

  /**
   * Retrieves email or phone on the basis of device id
   * Returns Future(None) if user does not exists and if user exists it returns Future(Some[(Option[phone_number], Option[email])))
   *
   * @param deviceId : user's device Id
   * @return Future[Option[(Option[String], Option[String])]]
   */
  def getEmailOrPhoneFromDeviceId(deviceId: Long): Future[Option[(Option[String], Option[String])]] = {
    logger.info(s"UserDAO: Fetching user's email and phone_number for device_id: $deviceId from spacebeaver.users_mapping table.")
    val query = userQuery
      .filter(col => col.deviceId === deviceId)
      .map(col => (col.phone, col.email))
      .result
      .headOption
    db.run(query)
  }

}
