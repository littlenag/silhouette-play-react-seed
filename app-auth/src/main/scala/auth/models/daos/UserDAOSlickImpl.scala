package auth.models.daos

import java.util.UUID

import auth.models.{ Registration, Settings, User }
import com.mohiva.play.silhouette.api.LoginInfo
import javax.inject.Inject
import play.api.libs.json.Json
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Give access to the [[User]] object.
 *
 * @param db The Database
 * @param ec The execution context.
 */
class UserDAOSlickImpl @Inject() (db: Database)(implicit ec: ExecutionContext) extends UserDAO with Tables {

  val profile: JdbcProfile = _root_.slick.jdbc.PostgresProfile

  import profile.api._

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
        case Some(info) => usersT.filter(_.id === info.id).result.headOption
      }
      loginInfos <- maybeUser match {
        case None       => DBIO.failed(new RuntimeException(s"No user found for extant login info: $loginInfo"))
        case Some(user) => loginInfoT.filter(_.id === user.id).result
      }
    } yield {
      maybeUser.map(userRow => fromRow(userRow, loginInfos))
    }

    db.run(actions.transactionally).recover {
      case ex =>
        //logger.info(ex)
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
      allInfo <- maybeUser match {
        case None       => DBIO.failed(new RuntimeException(s"No user with id: $userID"))
        case Some(user) => loginInfoT.filter(_.id === user.id).result
      }
    } yield {
      maybeUser.map(fromRow(_, allInfo))
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
    val (userRow, loginInfo) = toRow(user)

    val actions = for {
      _ <- usersT.insertOrUpdate(userRow)
      _ <- DBIO.sequence(loginInfo.map(loginInfoT.insertOrUpdate(_)))
    } yield {
      user
    }

    db.run(actions.transactionally)
  }

  private def toRow(user: User): (UserRow, Seq[LoginInfoRow]) = {
    (
      UserRow(
        user.id, user.name, user.email, user.avatarURL,
        user.registration.lang, user.registration.ip, user.registration.host,
        user.registration.userAgent, user.registration.activated, user.registration.dateTime,
        user.settings.lang, user.settings.timeZone
      ),
        user.loginInfo.map(r => LoginInfoRow(user.id, r.providerID, r.providerKey))
    )
  }

  private def fromRow(a: UserRow, b: Seq[LoginInfoRow]): User = {
    val registration = Registration(a.reg_lang, a.reg_ip, a.reg_host, a.reg_userAgent, a.reg_activated, a.reg_dateTime)
    val settings = Settings(a.lang, a.timeZone)
    val loginInfo = b.map(r => LoginInfo(r.providerID, r.providerKey))
    User(a.id, loginInfo, a.name, a.email, a.avatarURL, registration, settings)
  }

}
