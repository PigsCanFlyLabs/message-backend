package ca.pigscanfly.models

final case class AdminTokenParam(email: String, role: String)

case class JWTConfig(key: String,
                     expireDurationSec: Int,
                     expireDurationSecPassword: Int,
                     resetLink: String)

case class JWTTokenExtracts(exp: Long, iat: Long, email: String, role: String)

case class JWTPasswordTokenExtracts(exp: Long, iat: Long, email: String)
