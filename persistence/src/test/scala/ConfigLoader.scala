import com.typesafe.scalalogging.LazyLogging
import slick.jdbc.H2Profile
import slick.jdbc.JdbcBackend.Database

trait ConfigLoader extends LazyLogging {

  val driver = H2Profile

  val h2Url =
    "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE;INIT=RUNSCRIPT FROM './src/test/resources/create-databases.sql';"
  implicit val db: Database = Database.forURL(url = h2Url, driver = "org.h2.Driver")

}
