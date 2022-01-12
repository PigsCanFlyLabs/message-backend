package ca.pigscanfly.connections

import ca.pigscanfly.components._
import ca.pigscanfly.db.DatabaseApi.api._
import slick.lifted.{PrimaryKey, ProvenShape}

final case class UsersMapping(tag: Tag)(implicit val schema: String)
  extends Table[User](tag, Some(schema), "users_mapping") {

  def * : ProvenShape[User] =
    (deviceId,
      name,
      email,
      isDisabled).shaped <> (User.tupled, User.unapply)

  def deviceId: Rep[Int] = column[Int]("device_id")

  def name: Rep[String] = column[String]("name")

  def email: Rep[String] = column[String]("email")

  def isDisabled: Rep[Boolean] = column[Boolean]("is_disabled")

  implicit def primary: Rep[Int] = deviceId

  def pk: PrimaryKey = primaryKey("pk_a", deviceId)
}
