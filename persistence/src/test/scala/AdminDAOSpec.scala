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

  "AccountsDAOSpec service" should {
    "be able to createUserLoginDetails for user" in {
      whenReady(adminDao.checkIfAdminExists("email", "role")) { res =>
        res shouldBe 0
      }
    }
  }

}
