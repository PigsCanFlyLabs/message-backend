package ca.pigscanfly.components

final case class User(deviceId: Int,
                      name: String,
                      email: String,
                      isDisabled: Boolean)

final case class DisableUserRequest(deviceId: Int,
                                    email: String,
                                    isDisabled: Boolean)

final case class DeleteUserRequest(deviceId: Int,
                                   email: String)

