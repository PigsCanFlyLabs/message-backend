package ca.pigscanfly.models

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import io.circe.parser._

case class MessageCountsByDevice(messageCount: Int, fromDeviceMessageCount: Int, toDeviceMessageCount: Int, deviceType: Int, deviceId: Int)

case class MessageCountsByApplicationId(applicationId: Int, messageCount: Int, fromDeviceMessageCount: Int, toDeviceMessageCount: Int, messageCountsByDevice: List[MessageCountsByDevice])

case class MessageCount(totalMessageCount: Int, totalFromDeviceMessageCount: Int, totalToDeviceMessageCount: Int, messageCountsByApplicationId: List[MessageCountsByApplicationId])

object MessageCount {
  implicit val encoder: Encoder[MessageCount] = deriveEncoder[MessageCount]
  implicit val decoder: Decoder[MessageCount] = deriveDecoder[MessageCount]

  implicit val encoderMessageCountsByApplicationId: Encoder[MessageCountsByApplicationId] = deriveEncoder[MessageCountsByApplicationId]
  implicit val decoderMessageCountsByApplicationId: Decoder[MessageCountsByApplicationId] = deriveDecoder[MessageCountsByApplicationId]

  implicit val encoderMessageCountsByDevice: Encoder[MessageCountsByDevice] = deriveEncoder[MessageCountsByDevice]
  implicit val decoderMessageCountsByDevice: Decoder[MessageCountsByDevice] = deriveDecoder[MessageCountsByDevice]
}

//TODO Remove this JSON after End to End testing
//{
//  "totalMessageCount": 5,
//  "totalFromDeviceMessageCount": 4,
//  "totalToDeviceMessageCount": 1,
//  "messageCountsByApplicationId": [
//  {
//    "applicationId": 100,
//    "messageCount": 5,
//    "fromDeviceMessageCount": 4,
//    "toDeviceMessageCount": 1,
//    "messageCountsByDevice": [
//    {
//      "messageCount": 3,
//      "fromDeviceMessageCount": 2,
//      "toDeviceMessageCount": 1,
//      "deviceType": 1,
//      "deviceId": 1564
//    },
//    {
//      "messageCount": 2,
//      "fromDeviceMessageCount": 2,
//      "toDeviceMessageCount": 0,
//      "deviceType": 1,
//      "deviceId": 1565
//    }
//    ]
//  }
//  ]
//}
