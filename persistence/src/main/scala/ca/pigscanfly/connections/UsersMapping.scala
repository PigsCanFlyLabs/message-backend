package ca.pigscanfly.connections

import ca.pigscanfly.components._
import slick.jdbc.MySQLProfile.api._
import slick.lifted.{PrimaryKey, ProvenShape}

final case class UsersMapping(tag: Tag)(implicit val schema: String)
  extends Table[User](tag, Some(schema), "users_mapping") {

  def * : ProvenShape[User] =
    (deviceId,
      name,
      email,
      isDisabled).shaped <> (User.tupled, User.unapply)

  def name: Rep[String] = column[String]("name")

  def isDisabled: Rep[Boolean] = column[Boolean]("is_disabled")

  def pk: PrimaryKey = primaryKey("pk_a", (email, deviceId))

  def deviceId: Rep[String] = column[String]("device_id")

  implicit def primary: (Rep[String], Rep[String]) =
    (email, deviceId)

  def email: Rep[String] = column[String]("email")
}
