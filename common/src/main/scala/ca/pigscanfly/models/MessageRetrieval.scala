package ca.pigscanfly.models

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import io.circe.parser._

case class Message(packetId: Int,
                   deviceType: Int,
                   deviceId: Int,
                   deviceName: String,
                   dataType: Int,
                   userApplicationId: Int,
                   len: Int,
                   data: String,
                   ackPacketId: Int,
                   status: Int,
                   hiveRxTime: String)

case class MessageRetrieval(messageResponse: List[Message])

object MessageRetrieval {
  implicit val encoder: Encoder[MessageRetrieval] = deriveEncoder[MessageRetrieval]
  implicit val decoder: Decoder[MessageRetrieval] = deriveDecoder[MessageRetrieval]

  implicit val encoderMessage: Encoder[Message] = deriveEncoder[Message]
  implicit val decoderMessage: Decoder[Message] = deriveDecoder[Message]
}

//[
//{
//"packetId": 0,
//"deviceType": 0,
//"deviceId": 0,
//"deviceName": "string",
//"dataType": 0,
//"userApplicationId": 0,
//"len": 0,
//"data": "string",
//"ackPacketId": 0,
//"status": 0,
//"hiveRxTime": "2021-12-26T07:44:26.374Z"
//}
//]
