package ca.pigscanfly.models

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class Login(cookies: Map[String, String])

case class LoginCredentials(username: String, password: String)

object LoginCredentials {
  implicit val encoder: Encoder[LoginCredentials] = deriveEncoder[LoginCredentials]
  implicit val decoder: Decoder[LoginCredentials] = deriveDecoder[LoginCredentials]
}

object Login extends DefaultJsonProtocol {
  implicit val format: RootJsonFormat[Login] = jsonFormat1(Login.apply)
}