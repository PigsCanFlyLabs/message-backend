package ca.pigscanfly.components

final case class AdminLoginRequest(email: String,
                                   password: String)

final case class AdminLogin(email: String,
                            password: String,
                            role: String)