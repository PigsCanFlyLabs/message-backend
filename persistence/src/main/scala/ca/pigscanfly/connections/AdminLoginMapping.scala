package ca.pigscanfly.connections

import ca.pigscanfly.components._
import ca.pigscanfly.db.DatabaseApi.api._
import slick.lifted.ProvenShape

final case class AdminLoginMapping(tag: Tag)(implicit val schema: String)
  extends Table[AdminLogin](tag, Some(schema), "admin_login") {

  def * : ProvenShape[AdminLogin] =
    (email,
      password,
      role).shaped <> (AdminLogin.tupled, AdminLogin.unapply)

  def email: Rep[String] = column[String]("email")

  def password: Rep[String] = column[String]("password")

  def role: Rep[String] = column[String]("role")

}
