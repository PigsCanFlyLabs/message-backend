package ca.pigscanfly.util

import ca.pigscanfly.proto.messageEncryption._
import java.util.Base64

trait ProtoUtils {

  def encodeMessage(data: String): String =
    Base64.getEncoder.encodeToString(MessageData(data = data).toByteArray)

  def decodeMessage(data: String): MessageData =
    MessageData.parseFrom(Base64.getDecoder.decode(data))

}
