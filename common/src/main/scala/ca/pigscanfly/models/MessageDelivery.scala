package ca.pigscanfly.models

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}


case class MessageDelivery(packetId: Int, status: String)

object MessageDelivery extends DefaultJsonProtocol {
  implicit val encoder: Encoder[MessageDelivery] = deriveEncoder[MessageDelivery]
  implicit val decoder: Decoder[MessageDelivery] = deriveDecoder[MessageDelivery]
  implicit val format: RootJsonFormat[MessageDelivery] = jsonFormat2(MessageDelivery.apply)

}


