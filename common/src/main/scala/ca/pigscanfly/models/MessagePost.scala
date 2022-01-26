package ca.pigscanfly.models

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class MessagePost(deviceType: Int, deviceId: Long, userApplicationId: Int, data: String)

object MessagePost extends DefaultJsonProtocol {
  implicit val format: RootJsonFormat[MessagePost] = jsonFormat4(MessagePost.apply)
  implicit val encoder: Encoder[MessagePost] = deriveEncoder[MessagePost]
  implicit val decoder: Decoder[MessagePost] = deriveDecoder[MessagePost]
}
