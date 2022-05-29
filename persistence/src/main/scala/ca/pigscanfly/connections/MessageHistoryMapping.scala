package ca.pigscanfly.connections

import ca.pigscanfly.components._
import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape

import java.time.OffsetDateTime


final class MessageHistoryMapping(tag: Tag)
  extends Table[MessageHistory](tag, Some("spacebeaver"), "message_history") {

  def * : ProvenShape[MessageHistory] =
    (deviceId, to, sourceDestination, requestType, packetId, timestamp).shaped <> (MessageHistory.tupled, MessageHistory.unapply)

  def deviceId: Rep[Long] = column[Long]("device_id")

  def to: Rep[String] = column[String]("receiver")

  def sourceDestination: Rep[String] = column[String]("source_destination")

  def requestType: Rep[String] = column[String]("request_type")

  def packetId: Rep[Int] = column[Int]("packet_id")

  def timestamp: Rep[Option[OffsetDateTime]] = column[Option[OffsetDateTime]]("date_time")

}