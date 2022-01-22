package ca.pigscanfly.util

import ca.pigscanfly.proto.MessageDataPB.MessageDataPB

trait ProtoUtils {

  def encodePostMessage(messageDataPB: MessageDataPB): String =
    java.util.Base64.getEncoder.encodeToString(messageDataPB.toByteArray)

//  def decodeMessage(data: String): MessageData =
//    MessageData.parseFrom(Base64.getDecoder.decode(data))

}
