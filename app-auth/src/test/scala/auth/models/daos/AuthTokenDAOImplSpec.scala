package auth.models.daos

import java.time.Instant
import java.util.UUID

import auth.models.AuthToken
import db.utils.SlickSession
import play.api.test.{ PlaySpecification, WithApplication }
import test.DbSpecification

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Test case for the [[AuthTokenDAOImpl]] class.
 */
class AuthTokenDAOImplSpec extends PlaySpecification with DbSpecification {

  "The `find` method" should {
    "find a token for the given ID" in new WithDb with Context {
      val result = await(dao.find(id))

      result must beSome(token)
    }

    "return None if no auth info for the given login info exists" in new WithDb with Context {
      val result = await(dao.find(UUID.randomUUID()))

      result should beNone
    }
  }

  "The `findExpired` method" should {
    "find expired tokens" in new WithDb with Context {
      val result = await(dao.findExpired(token.expiry.plusSeconds(5)))

      result must be equalTo Seq(token)
    }
  }

  "The `save` method" should {
    "insert a new token" in new WithDb with Context {
      val newToken = token.copy(id = UUID.randomUUID())

      await(dao.save(newToken)) must be equalTo newToken
      await(dao.find(newToken.id)) must beSome(newToken)
    }

    "update an existing token" in new WithDb with Context {
      val updatedToken = token.copy(expiry = Instant.now())

      await(dao.save(updatedToken)) must be equalTo updatedToken
      await(dao.find(token.id)) must beSome(updatedToken)
    }
  }

  "The `remove` method" should {
    "remove a token" in new WithDb with Context {
      await(dao.remove(id))
      await(dao.find(id)) must beNone
    }
  }

  /**
   * The context.
   */
  trait Context extends DbScope {
    self: WithApplication =>

    import session.profile.api._

    /**
     * The test fixtures to insert.
     */
    override val actions = DBIO.seq(
      sqlu"""INSERT INTO "auth_tokens" VALUES
            ('bdd4e520-8803-4f7d-ab67-9b50c12e9919', 'c0a68d68-e118-4068-844c-8f420b71985e', to_timestamp(1493826799));"""
    )

    /**
     * The auth token DAO implementation.
     */
    val dao = new AuthTokenDAOImpl(app.injector.instanceOf[SlickSession])

    /**
     * An ID for the stored token.
     */
    val id = UUID.fromString("bdd4e520-8803-4f7d-ab67-9b50c12e9919")

    /**
     * The stored auth token.
     */
    val token = AuthToken(
      id = id,
      userID = UUID.fromString("c0a68d68-e118-4068-844c-8f420b71985e"),
      expiry = Instant.ofEpochSecond(1493826799L)
    )
  }
}
