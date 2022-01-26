package ca.pigscanfly.util

trait Validations {

  val NUMBER="number"
  val EMAIl="email"
  private val emailRegex =
    """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r


  def validEmailPhone(string: String): Boolean ={
    if(string forall Character.isDigit) true
    else if(isValidEmail(string)) true
    else false
  }

  def isValidEmail(e: String): Boolean = e match {
    case e if e.trim.isEmpty                           => false
    case e if emailRegex.findFirstMatchIn(e).isDefined => true
    case _                                             => false
  }

}
