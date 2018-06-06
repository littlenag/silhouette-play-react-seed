package auth.models.daos

import java.time.Instant
import java.util.UUID

import auth.models.{ Registration, Settings, User }
import com.mohiva.play.silhouette.api.LoginInfo
import db.utils.SlickSession
import play.api.i18n.Lang
import play.api.test.{ PlaySpecification, WithApplication }
import test.DbSpecification

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Test case for the [[UserDAOImpl]] class.
 */
class UserDAOImplSpec extends PlaySpecification with DbSpecification {

  "The `find` method" should {
    "find a user for the given login info" in new WithDb with Context {
      val result = await(dao.find(loginInfo))

      result must beSome(user)
    }

    "find a user for the given ID" in new WithDb with Context {
      val result = await(dao.find(userID))

      result must beSome(user)
    }

    "return None if no user for the given login info exists" in new WithDb with Context {
      val result = await(dao.find(LoginInfo("test", "test")))

      result should beNone
    }

    "return None if no user for the given ID exists" in new WithDb with Context {
      val result = await(dao.find(UUID.randomUUID()))

      result should beNone
    }
  }

  "The `save` method" should {
    "insert a user" in new WithDb with Context {
      val newUser = user.copy(id = UUID.randomUUID())

      await(dao.save(newUser)) must be equalTo newUser
      await(dao.find(newUser.id)) must beSome(newUser)
    }

    "update an existing user" in new WithDb with Context {
      val updatedUser = user.copy(name = Some("Jane Doe"))

      await(dao.save(updatedUser)) must be equalTo updatedUser
      await(dao.find(user.id)) must beSome(updatedUser)
    }
  }

  /**
   * The context.
   */
  trait Context extends DbScope {
    self: WithApplication =>

    /**
     * The test fixtures to insert.
     */
    override val fixtures = Seq(
      "models/daos/users/user.sql"
    )

    /**
     * The user DAO implementation.
     */
    val dao = new UserDAOImpl(app.injector.instanceOf[SlickSession])

    /**
     * A userID for the stored user.
     */
    val userID = UUID.fromString("c0a68d68-e118-4068-844c-8f420b71985e")

    /**
     * A login info for the stored user.
     */
    val loginInfo = LoginInfo("credentials", "john@doe.com")

    /**
     * The stored user.
     */
    val user = User(
      id = userID,
      loginInfo = loginInfo,
      name = Some("John Doe"),
      email = Some("john@doe.com"),
      avatarURL = None,
      registration = Registration(
        lang = Lang("en-US"),
        ip = "127.0.0.1",
        host = Some("localhost:9000"),
        userAgent = Some("Chrome/58.0.3029.81 Safari/537.36"),
        activated = true,
        dateTime = Instant.ofEpochSecond(1493826799L)
      ),
      settings = Settings(
        lang = Lang("en-US")
      )
    )
  }
}
