package ca.pigscanfly.connections

import ca.pigscanfly.components._
import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape

final case class UsersMapping(tag: Tag)
  extends Table[User](tag, Some("spacebeaver"), "users_mapping") {

  def * : ProvenShape[User] =
    (deviceId,
      phone,
      email,
      isDisabled).shaped <> (User.tupled, User.unapply)

  def phone: Rep[String] = column[String]("phone_number")

  def isDisabled: Rep[Boolean] = column[Boolean]("is_disabled")

  def deviceId: Rep[Long] = column[Long]("device_id")

  def email: Rep[String] = column[String]("email", O.PrimaryKey)
}
