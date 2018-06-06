package auth.models.daos

import java.util.UUID

import auth.models.{ Registration, Settings, User }
import com.mohiva.play.silhouette.api.LoginInfo
import db.utils.Tables
import javax.inject.Inject
import db.utils.SlickSession

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Give access to the [[User]] object.
 *
 * @param session The SlickSession
 * @param ec The execution context.
 */
class UserDAOImpl @Inject() (val session: SlickSession)(implicit ec: ExecutionContext) extends UserDAO with Tables {

  import play.api.Logger

  import session.profile.api._

  /**
   * Finds a user by their login info.
   *
   * @param loginInfo The login info of the user to find.
   * @return The found user or None if no user for the given login info could be found.
   */
  def find(loginInfo: LoginInfo): Future[Option[User]] = {
    val actions = for {
      maybeInfo <- loginInfoT.filter(r => r.providerId === loginInfo.providerID && r.providerKey === loginInfo.providerKey).result.headOption
      maybeUser <- maybeInfo match {
        case None       => DBIO.failed(new RuntimeException(s"Login info not found: $loginInfo"))
        case Some(info) => usersT.filter(_.id === info.userId).result.headOption
      }
    } yield {
      for {
        user <- maybeUser
        info <- maybeInfo
      } yield fromRow(user, info)
    }

    db.run(actions.transactionally).recover {
      case ex =>
        Logger.info(s"Issue finding $loginInfo", ex)
        None
    }
  }

  /**
   * Finds a user by its user ID.
   *
   * @param userID The ID of the user to find.
   * @return The found user or None if no user for the given ID could be found.
   */
  def find(userID: UUID): Future[Option[User]] = {
    val actions = for {
      maybeUser <- usersT.filter(_.id === userID).result.headOption
      maybeInfo <- maybeUser match {
        case None       => DBIO.failed(new RuntimeException(s"No user with id: $userID"))
        case Some(user) => loginInfoT.filter(_.userId === user.id).result.headOption
      }
    } yield {
      for {
        user <- maybeUser
        info <- maybeInfo
      } yield fromRow(user, info)
    }

    db.run(actions.transactionally).recover {
      case ex =>
        //logger.info(ex)
        None
    }
  }

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User): Future[User] = {
    val (userRow, loginInfoRow) = toRow(user)

    val actions = for {
      _ <- usersT.insertOrUpdate(userRow)

      maybeLoginInfo <- loginInfoT.filter(r =>
        r.userId === loginInfoRow.userId &&
          r.providerId === loginInfoRow.providerId &&
          r.providerKey === loginInfoRow.providerKey).result.headOption

      _ <- maybeLoginInfo match {
        case None =>
          val li = loginInfoRow.copy(id = Some(UUID.randomUUID()))
          (loginInfoT += li) andThen DBIO.successful(li)
        case Some(lir) =>
          DBIO.successful(lir)
      }
    } yield {
      user
    }

    db.run(actions.transactionally)
  }

  private def toRow(user: User): (UserRow, LoginInfoRow) = {
    (
      UserRow(
        user.id, user.name, user.email, user.avatarURL,
        user.registration.lang, user.registration.ip, user.registration.host,
        user.registration.userAgent, user.registration.activated, user.registration.dateTime,
        user.settings.lang, user.settings.timeZone
      ),
        LoginInfoRow(None, user.id, user.loginInfo.providerID, user.loginInfo.providerKey)
    )
  }

  private def fromRow(a: UserRow, b: LoginInfoRow): User = {
    val registration = Registration(a.reg_lang, a.reg_ip, a.reg_host, a.reg_userAgent, a.reg_activated, a.reg_dateTime)
    val settings = Settings(a.lang, a.timeZone)
    User(a.id, LoginInfo(b.providerId, b.providerKey), a.name, a.email, a.avatarURL, registration, settings)
  }

}
