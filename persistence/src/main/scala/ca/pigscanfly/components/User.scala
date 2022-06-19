package ca.pigscanfly.components

import java.time.OffsetDateTime

final case class User(customerId: Option[String],
                      deviceId: Long,
                      phone: Option[String],
                      email: Option[String],
                      isDisabled: Boolean)

final case class UpdateUserRequest(deviceId: Long,
                                   phone: Option[String],
                                   email: Option[String])

final case class DisableUserRequest(deviceId: Long,
                                    isDisabled: Boolean)

final case class DeleteUserRequest(deviceId: Long)

