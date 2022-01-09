package ca.pigscanfly.components

final case class User(deviceId: String,
                      name: String,
                      email: String,
                      isDisabled: Boolean)

final case class DisableUserRequest(deviceId: String,
                                    email: String,
                                    isDisabled: Boolean)

final case class DeleteUserRequest(deviceId: String,
                                   email: String)

