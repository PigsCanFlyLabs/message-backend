package ca.pigscanfly.components

final case class User(deviceId: Long,
                      phone: Option[String],
                      email: Option[String],
                      isDisabled: Boolean)

final case class UpdateUserRequest(deviceId: Long,
                                   phone: Option[String],
                                   email: Option[String])

final case class DisableUserRequest(deviceId: Long,
                                    isDisabled: Boolean)

final case class DeleteUserRequest(deviceId: Long)

