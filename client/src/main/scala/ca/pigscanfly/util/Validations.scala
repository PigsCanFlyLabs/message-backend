package ca.pigscanfly.util

import ca.pigscanfly.util.Constants.{EMAIl, SMS, UNKOWN}

trait Validations {

  private val emailRegex =
    """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r


  def validateEmailPhone(string: String): Boolean = {
    if(validatePhone(string)) true
    else if(isValidEmail(string)) true
    else false
  }

  def detectSource(string: String): String ={
    if(validatePhone(string)) SMS
    else if(isValidEmail(string)) EMAIl
    else UNKOWN
  }

  def validatePhone(string: String): Boolean ={
    if(string forall Character.isDigit) true
    else false
  }

  def isValidEmail(e: String): Boolean = e match {
    case e if e.trim.isEmpty                           => false
    case e if emailRegex.findFirstMatchIn(e).isDefined => true
    case _                                             => false
  }

}
