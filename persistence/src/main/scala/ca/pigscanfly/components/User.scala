package ca.pigscanfly.components

final case class User(deviceId: Long,
                      phone: Option[String],
                      email: Option[String],
                      isDisabled: Boolean)

final case class DisableUserRequest(deviceId: Long,
                                    email: String,
                                    isDisabled: Boolean)

final case class DeleteUserRequest(deviceId: Long,
                                   email: String)

