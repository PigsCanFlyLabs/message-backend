package ca.pigscanfly.components

import java.time.OffsetDateTime

case class MessageHistory(deviceId: Long,
                          to: String,
                          sourceDestination: String,
                          requestType: String,
                          packetId: Int,
                          timestamp: Option[OffsetDateTime] = Some(OffsetDateTime.now()))
