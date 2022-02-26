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

  def checkIfUserExists(email: String, deviceId: Long): Future[Int] = {
    val query = userQuery
      .filter(col => col.email === email &&
        col.deviceId === deviceId)
      .size
      .result
    db.run(query)
  }

  def getUserDetails(deviceId: Long): Future[Option[User]] = {
    val query = userQuery
      .filter(col => col.deviceId === deviceId)
      .result
      .headOption
    db.run(query)
  }

  def insertUserDetails(user: User): Future[Int] = db.run(userQuery += user)

  def updateUserDetails(user: User): Future[Int] = {
    val query = userQuery
      .filter(col => col.email === user.email &&
        col.deviceId === user.deviceId)
      .map(col => (col.phone, col.isDisabled))
      .update((Some(user.phone), user.isDisabled))
    db.run(query)
  }

  def disableUser(request: DisableUserRequest): Future[Int] = {
    val query = userQuery
      .filter(col => col.email === request.email &&
        col.deviceId === request.deviceId)
      .map(_.isDisabled)
      .update(request.isDisabled)
    db.run(query)
  }

  def deleteUser(request: DeleteUserRequest): Future[Int] = {
    val query = userQuery
      .filter(col => col.email === request.email &&
        col.deviceId === request.deviceId)
      .delete
    db.run(query)
  }

  def getDeviceIdFromEmailOrPhone(from: String): Future[Option[Long]] = {
    val query = userQuery
      .filter(col => col.email === from || col.phone === from)
      .map(_.deviceId)
      .result
      .headOption
    db.run(query)
  }

  def getEmailOrPhoneFromDeviceId(deviceId: Long): Future[Option[(Option[String], Option[String])]] = {
    val query = userQuery
      .filter(col => col.deviceId === deviceId)
      .map(col => (col.phone, col.email))
      .result
      .headOption
    db.run(query)
  }

}
