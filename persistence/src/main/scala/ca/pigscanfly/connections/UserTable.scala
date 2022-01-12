package ca.pigscanfly.connections

import ca.pigscanfly.components._
import ca.pigscanfly.db.DatabaseApi.api._
import slick.lifted.ProvenShape

final case class UserTable(tag: Tag)(implicit val schema: String)
  extends Table[User](tag, Some(schema), "user_details") {


  def * : ProvenShape[User] =
    (deviceId,
      name,
      email,
      isActive).shaped <> (User.tupled, User.unapply)

  def deviceId: Rep[Int] = column[Int]("device_id", O.PrimaryKey)

  def name: Rep[String] = column[String]("name")

  def email: Rep[String] = column[String]("email")

  def isActive: Rep[Boolean] = column[Boolean]("is_active")
}
