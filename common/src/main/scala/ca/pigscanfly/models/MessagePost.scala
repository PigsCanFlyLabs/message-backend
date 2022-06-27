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

case class ScheduleSendMessageRequest(customerId: String, deviceId: Long, to: String, data: String)
object ScheduleSendMessageRequest extends DefaultJsonProtocol {
  implicit val format: RootJsonFormat[ScheduleSendMessageRequest] = jsonFormat4(ScheduleSendMessageRequest.apply)
  implicit val encoder: Encoder[ScheduleSendMessageRequest] = deriveEncoder[ScheduleSendMessageRequest]
  implicit val decoder: Decoder[ScheduleSendMessageRequest] = deriveDecoder[ScheduleSendMessageRequest]
}