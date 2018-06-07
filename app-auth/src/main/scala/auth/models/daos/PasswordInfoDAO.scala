package auth.models.daos

import auth.models.AuthToken
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import db.utils.{ SlickSession, TableBase, Tables }
import javax.inject.Inject

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Give access to the [[AuthToken]] object.
 *
 * @param slickSession  The SlickSession.
 * @param ec            The execution context.
 */
class PasswordInfoDAO @Inject() (val slickSession: SlickSession)(implicit ec: ExecutionContext) extends DelegableAuthInfoDAO[PasswordInfo] with Tables with TableBase {

  import profile.api._

  /**
   * Finds the auth info which is linked to the specified login info.
   *
   * @param loginInfo The linked login info.
   * @return The found auth info or None if no auth info could be found for the given login info.
   */
  def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
    val actions = for {
      maybeInfo <- loginInfoT.filter(r => r.providerId === loginInfo.providerID && r.providerKey === loginInfo.providerKey).result.headOption
      maybePassword <- maybeInfo match {
        case None       => DBIO.failed(new RuntimeException(s"Login info not found: $loginInfo"))
        case Some(info) => passwordInfoT.filter(_.loginInfoId === info.id).result.headOption
      }
    } yield {
      maybePassword.map(fromRow)
    }

    db.run(actions.transactionally)
  }

  /**
   * Adds new auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be added.
   * @param authInfo The auth info to add.
   * @return The added auth info.
   */
  def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    val actions = for {
      maybeLoginInfo <- loginInfoT.filter(r => r.providerId === loginInfo.providerID && r.providerKey === loginInfo.providerKey).result.headOption
      _ <- maybeLoginInfo match {
        case None      => DBIO.failed(new RuntimeException(s"Login info not found: $loginInfo"))
        case Some(lir) => passwordInfoT += toRow(authInfo).copy(loginInfoId = lir.id)
      }
    } yield {
      authInfo
    }

    db.run(actions.transactionally)
  }

  /**
   * Updates the auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be updated.
   * @param authInfo The auth info to update.
   * @return The updated auth info.
   */
  def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    val actions = for {
      maybeLoginInfo <- loginInfoT.filter(r => r.providerId === loginInfo.providerID && r.providerKey === loginInfo.providerKey).result.headOption
      _ <- maybeLoginInfo match {
        case None =>
          DBIO.failed(new RuntimeException(s"Login info not found: $loginInfo"))
        case Some(lir) =>
          passwordInfoT
            .filter(_.loginInfoId === lir.userId)
            .map(r => (r.hasher, r.password, r.salt))
            .update((authInfo.hasher, authInfo.password, authInfo.salt))
      }
    } yield {
      authInfo
    }

    db.run(actions.transactionally)
  }

  /**
   * Saves the auth info for the given login info.
   *
   * This method either adds the auth info if it doesn't exists or it updates the auth info
   * if it already exists.
   *
   * @param loginInfo The login info for which the auth info should be saved.
   * @param authInfo The auth info to save.
   * @return The saved auth info.
   */
  def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    val actions = for {
      maybeLoginInfo <- loginInfoT.filter(r => r.providerId === loginInfo.providerID && r.providerKey === loginInfo.providerKey).result.headOption
      _ <- maybeLoginInfo match {
        case None      => DBIO.failed(new RuntimeException(s"Login info not found: $loginInfo"))
        case Some(lir) => passwordInfoT.insertOrUpdate(toRow(authInfo).copy(loginInfoId = lir.id))
      }
    } yield {
      authInfo
    }

    db.run(actions.transactionally)
  }

  /**
   * Removes the auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be removed.
   * @return A future to wait for the process to be completed.
   */
  def remove(loginInfo: LoginInfo): Future[Unit] = {
    val actions = loginInfoT.filter(r => r.providerId === loginInfo.providerID && r.providerKey === loginInfo.providerKey).delete

    db.run(actions.transactionally andThen DBIO.successful(()))
  }

  private def toRow(a: PasswordInfo): PasswordInfoRow = {
    PasswordInfoRow(None, a.hasher, a.password, a.salt)
  }

  private def fromRow(a: PasswordInfoRow): PasswordInfo = {
    PasswordInfo(a.hasher, a.password, a.salt)
  }
}
