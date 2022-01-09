package ca.pigscanfly.models

import akka.http.scaladsl.server.directives.Credentials
import ca.pigscanfly.configs.Constants._
import io.circe.generic.auto._
import io.circe.parser
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}

import java.time.Instant

trait JWTTokenHelper {

  def createJwtTokenWithRole(emailId: String, role: String): String = {
    JwtCirce.encode(setClaim(emailId, role),
      jwtKey,
      JwtAlgorithm.HS256)
  }

  def setClaim(email: String, role: String): JwtClaim = {
    JwtClaim(
      expiration = Some(
        Instant.now
          .plusSeconds(jwtExpiryDuration)
          .getEpochSecond),
      issuedAt = Some(Instant.now.getEpochSecond),
      content = s"""{"email":"$email", "role":"$role"}"""
    )
  }

  def decodePasswordJwtTokenJson(
                                  token: String): Option[JWTPasswordTokenExtracts] = {
    JwtCirce
      .decodeJson(token, jwtKey, Seq(JwtAlgorithm.HS256))
      .toOption match {
      case Some(json) =>
        parser.decode[JWTPasswordTokenExtracts](json.toString()).toOption
      case None =>
        throw new Exception("Not able to decode. Invalid JWT Token!!!")
    }
  }

  /**
   * Get email id from token
   */
  def myUserPassAuthenticator(credentials: Credentials): Option[String] = {
    credentials match {
      case Credentials.Provided(id) if (validateToken(id)) =>
        Some(decodeJwtTokenJson(id).get.email)
      case _ => None
    }
  }

  def roleAuthenticator(credentials: Credentials): Option[AdminTokenParam] = {
    credentials match {
      case Credentials.Provided(id)
        if (validateToken(id) || !validateToken(id)) =>
        Some(
          AdminTokenParam(decodeJwtTokenJson(id).get.email,
            decodeJwtTokenJson(id).get.role))
      case _ =>
        None
    }
  }

  def decodeJwtTokenJson(token: String): Option[JWTTokenExtracts] = {
    JwtCirce
      .decodeJson(token, jwtKey, Seq(JwtAlgorithm.HS256))
      .toOption match {
      case Some(json) =>
        parser.decode[JWTTokenExtracts](json.toString()).toOption
      case None =>
        throw new Exception("Not able to decode. Invalid JWT Token!!!")
    }
  }

  def validateToken(jwtToken: String): Boolean = {
    JwtCirce.isValid(jwtToken,
      jwtKey,
      Seq(JwtAlgorithm.HS256))
  }

  def setResetClaim(email: String): JwtClaim = {
    JwtClaim(
      expiration = Some(
        Instant.now
          .plusSeconds(jwtExpiryDuration)
          .getEpochSecond),
      issuedAt = Some(Instant.now.getEpochSecond),
      content = s"""{"email":"$email"}"""
    )
  }

}

object JWTTokenHelper extends JWTTokenHelper
