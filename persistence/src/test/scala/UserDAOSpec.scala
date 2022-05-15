import ca.pigscanfly.components.{DeleteUserRequest, DisableUserRequest, UpdateUserRequest, User}
import ca.pigscanfly.dao.UserDAO
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{AsyncWordSpecLike, Matchers}

import java.util.UUID
import scala.language.postfixOps

class UserDAOSpec extends AsyncWordSpecLike with ScalaFutures with Matchers with ConfigLoader {

  implicit val searchLimit: Int = 2
  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  val userDao = new UserDAO()

  val user: User = User(customerId=Some(UUID.randomUUID().toString),deviceId = 1L, phone = Some("9876543210"), email = Some("email@domain.com"), isDisabled = false)
  val updateUser: UpdateUserRequest = UpdateUserRequest(deviceId = 1L, phone = Some("9876543210"), email = Some("email@domain.com"))
  val disableUserRequest: DisableUserRequest = DisableUserRequest(deviceId = user.deviceId, isDisabled = true)
  val deleteUserRequest: DeleteUserRequest = DeleteUserRequest(deviceId = user.deviceId)

  "UserDAOSpec service" should {

    "not be able to check If User Exists" in {
      whenReady(userDao.checkIfUserExists(Some("email"), 0L)) { res =>
        res shouldBe 0
      }
    }

    "not be able to get User Details" in {
      whenReady(userDao.getUserDetails(0L)) { res =>
        res shouldBe None
      }
    }

    "not be able to get Device Id From Email Or Phone" in {
      whenReady(userDao.getDeviceIdFromEmailOrPhone("email")) { res =>
        res shouldBe None
      }
    }

    "not be able to getEmailOrPhoneFromDeviceId" in {
      for {
        res <- userDao.getEmailOrPhoneFromDeviceId(0L)
      } yield {
        res shouldBe None
      }
    }

    "not be able to disableUser" in {
      whenReady(userDao.disableUser(disableUserRequest)) { res =>
        res shouldBe 0
      }
    }

    "not be able to deleteUser" in {
      whenReady(userDao.deleteUser(deleteUserRequest)) { res =>
        res shouldBe 0
      }
    }

    "be able to insert user details" in {
      whenReady(userDao.insertUserDetails(user)) { res =>
        res shouldBe 1
      }
    }

    "be able to check If User Exists" in {
      whenReady(userDao.checkIfUserExists(user.email, user.deviceId)) { res =>
        res shouldBe 1
      }
    }

    "be able to get User Details" in {
      whenReady(userDao.getUserDetails(user.deviceId)) { res =>
        res shouldBe Some(user)
      }
    }

    "be able to getEmailOrPhoneFromDeviceId" in {
      for {
        res <- userDao.getEmailOrPhoneFromDeviceId(user.deviceId)
      } yield {
        res shouldBe Some(user.phone, user.email)
      }
    }

    "be able to updateUserDetails" in {
      whenReady(userDao.updateUserDetails(updateUser.copy(phone = Some("0000000000")))) { res =>
        res shouldBe 1
      }
    }

    "be able to disableUser" in {
      whenReady(userDao.disableUser(disableUserRequest)) { res =>
        res shouldBe 1
      }
    }

    "be able to get Device Id From Email Or Phone" in {
      whenReady(userDao.getDeviceIdFromEmailOrPhone(user.email.getOrElse(""))) { res =>
        res shouldBe Some(user.deviceId)
      }
    }

    "be able to deleteUser" in {
      whenReady(userDao.deleteUser(deleteUserRequest)) { res =>
        res shouldBe 1
      }
    }


  }

}
