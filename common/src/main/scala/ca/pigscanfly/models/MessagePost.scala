package ca.pigscanfly.models

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import io.circe.parser._

case class MessagePost(deviceType: Int, deviceId: Int, userApplicationId: Int, data: String)

object MessagePost {
  implicit val encoder: Encoder[MessagePost] = deriveEncoder[MessagePost]
  implicit val decoder: Decoder[MessagePost] = deriveDecoder[MessagePost]
}
