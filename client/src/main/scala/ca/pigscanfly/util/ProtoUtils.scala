package ca.pigscanfly.util

import ca.pigscanfly.proto.MessageDataPB.MessageDataPB

import java.util.Base64

trait ProtoUtils {

  def encodePostMessage(messageDataPB: MessageDataPB): String =
    java.util.Base64.getEncoder.encodeToString(messageDataPB.toByteArray)

  def decodeGetMessage(data: String): MessageDataPB =
    MessageDataPB.parseFrom(Base64.getDecoder.decode(data))

  //  decodeGetMessage("123564").message.map(x=> x.fromOrTo)

}
