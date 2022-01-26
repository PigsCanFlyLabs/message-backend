package ca.pigscanfly.models

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}


case class GetMessage(packetId: Int,
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

object GetMessage extends DefaultJsonProtocol {
  implicit val format: RootJsonFormat[GetMessage] = jsonFormat11(GetMessage.apply)
}

case class MessageRetrieval(messageResponse: List[GetMessage])

object MessageRetrieval extends DefaultJsonProtocol {
  implicit val format: RootJsonFormat[MessageRetrieval] = jsonFormat1(MessageRetrieval.apply)

  implicit val encoder: Encoder[MessageRetrieval] = deriveEncoder[MessageRetrieval]
  implicit val decoder: Decoder[MessageRetrieval] = deriveDecoder[MessageRetrieval]

  implicit val encoderMessage: Encoder[GetMessage] = deriveEncoder[GetMessage]
  implicit val decoderMessage: Decoder[GetMessage] = deriveDecoder[GetMessage]
}
