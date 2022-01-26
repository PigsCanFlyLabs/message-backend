package ca.pigscanfly.components

final case class User(deviceId: Long,
                      phone: String,
                      email: String,
                      isDisabled: Boolean)

final case class DisableUserRequest(deviceId: Long,
                                    email: String,
                                    isDisabled: Boolean)

final case class DeleteUserRequest(deviceId: Long,
                                   email: String)

