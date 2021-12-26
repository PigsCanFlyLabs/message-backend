package ca.pigscanfly.models

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import io.circe.parser._

case class MessageDelivery(packetId: Int, status: String)

object MessageDelivery {
  implicit val encoder: Encoder[MessageDelivery] = deriveEncoder[MessageDelivery]
  implicit val decoder: Decoder[MessageDelivery] = deriveDecoder[MessageDelivery]
}

//{
//  "packetId": 1111111,
//  "status": "OK"
//}

