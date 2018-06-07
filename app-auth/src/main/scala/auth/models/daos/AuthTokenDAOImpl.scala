package auth.models.daos

import java.time.Instant
import java.util.UUID

import auth.models.AuthToken
import db.utils.{ SlickSession, TableBase, Tables }
import javax.inject.Inject

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Give access to the [[AuthToken]] object.
 *
 * @param slickSession  The SlickSession.
 * @param ec            The execution context.
 */
class AuthTokenDAOImpl @Inject() (val slickSession: SlickSession)(implicit ec: ExecutionContext) extends AuthTokenDAO with Tables with TableBase {

  import profile.api._

  /**
   * Finds a token by its ID.
   *
   * @param id The unique token ID.
   * @return The found token or None if no token for the given ID could be found.
   */
  def find(id: UUID): Future[Option[AuthToken]] =
    db.run(authTokensT.filter(_.id === id).result.headOption.map(_.map(fromRow)))

  /**
   * Finds expired tokens.
   *
   * @param instant The current instant.
   */
  def findExpired(instant: Instant): Future[Seq[AuthToken]] =
    db.run(authTokensT.filter(_.expiry <= instant).result.map(_.map(fromRow)))

  /**
   * Saves a token.
   *
   * If the token doesn't exists then it will be added, otherwise it will be updated.
   *
   * @param token The token to save.
   * @return The saved token.
   */
  def save(token: AuthToken): Future[AuthToken] =
    db.run(authTokensT.insertOrUpdate(toRow(token)) andThen DBIO.successful(token))

  /**
   * Removes the token for the given ID.
   *
   * @param id The ID for which the token should be removed.
   * @return A future to wait for the process to be completed.
   */
  def remove(id: UUID): Future[Unit] =
    db.run(authTokensT.filter(_.id === id).delete andThen DBIO.successful(()))

  private def toRow(a: AuthToken): AuthTokenRow = {
    AuthTokenRow(a.id, a.userID, a.expiry)
  }

  private def fromRow(a: AuthTokenRow): AuthToken = {
    AuthToken(a.id, a.userId, a.expiry)
  }
}
