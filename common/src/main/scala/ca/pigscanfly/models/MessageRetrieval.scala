package ca.pigscanfly.models

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}


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

object Message extends DefaultJsonProtocol {
  implicit val format: RootJsonFormat[Message] = jsonFormat11(Message.apply)
}

case class MessageRetrieval(messageResponse: List[Message])

object MessageRetrieval extends DefaultJsonProtocol {
  implicit val format: RootJsonFormat[MessageRetrieval] = jsonFormat1(MessageRetrieval.apply)

  implicit val encoder: Encoder[MessageRetrieval] = deriveEncoder[MessageRetrieval]
  implicit val decoder: Decoder[MessageRetrieval] = deriveDecoder[MessageRetrieval]

  implicit val encoderMessage: Encoder[Message] = deriveEncoder[Message]
  implicit val decoderMessage: Decoder[Message] = deriveDecoder[Message]
}
