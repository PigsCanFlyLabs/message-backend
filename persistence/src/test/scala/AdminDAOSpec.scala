import ca.pigscanfly.components.AdminLogin
import ca.pigscanfly.dao.AdminDAO
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{AsyncWordSpecLike, Matchers}

class AdminDAOSpec extends AsyncWordSpecLike with ScalaFutures with Matchers with ConfigLoader {

  implicit val searchLimit: Int = 2
  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  implicit val schema: String = "spacebeaver_admin"

  val adminDao = new AdminDAO()

  val adminLoginRequest = AdminLogin("email", "password", "role")

  "AccountsDAOSpec service" should {

    "be able to get resource permissions" in {
      whenReady(adminDao.getResourcePermissions) { res =>
        res shouldBe(Seq(), Seq())
      }
    }

    "not be able to check if admin exists" in {
      whenReady(adminDao.checkIfAdminExists("email", "role")) { res =>
        res shouldBe 0
      }
    }

    "be able to create admin login" in {
      whenReady(adminDao.createAdminUser(adminLoginRequest)) { res =>
        res shouldBe 1
      }
    }

    "be able to validate admin login" in {
      whenReady(adminDao.validateAdminLogin(adminLoginRequest)) { res =>
        res shouldBe 1
      }
    }

  }

}
