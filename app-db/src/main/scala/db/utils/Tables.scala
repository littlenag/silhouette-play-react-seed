package db.utils

import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

import play.api.i18n.Lang

// scalastyle:off
trait Tables {
  val session: SlickSession

  import session.profile.api._
  lazy val db = session.db

  implicit val instantColumnType = MappedColumnType.base[Instant, Timestamp](
    instant => new Timestamp(instant.toEpochMilli),
    timestamp => Instant.ofEpochMilli(timestamp.getTime)
  )

  implicit val langColumnType = MappedColumnType.base[Lang, String](
    lang => lang.code,
    code => Lang(code)
  )

  case class AuthTokenRow(id: UUID, userId: UUID, expiry: Instant)

  class AuthTokens(tag: Tag) extends Table[AuthTokenRow](tag, "auth_tokens") {
    def id = column[UUID]("id", O.PrimaryKey)
    def userId = column[UUID]("userId")
    def expiry = column[Instant]("expiry")
    def * = (id, userId, expiry) <> (AuthTokenRow.tupled, AuthTokenRow.unapply)
  }

  /**
   * A UserRow object.
   *
   * @param id           The unique ID of the user.
   * @param name         Maybe the name of the authenticated user.
   * @param email        Maybe the email of the authenticated provider.
   * @param avatarURL    Maybe the avatar URL of the authenticated provider.
   */
  case class UserRow(
    id: UUID,
    name: Option[String],
    email: Option[String],
    avatarURL: Option[String],
    // registration
    reg_lang: Lang,
    reg_ip: String,
    reg_host: Option[String],
    reg_userAgent: Option[String],
    reg_activated: Boolean,
    reg_dateTime: Instant,
    // settings
    lang: Lang,
    timeZone: Option[String] = None
  )

  class Users(tag: Tag) extends Table[UserRow](tag, "users") {
    def id = column[UUID]("id", O.PrimaryKey)
    def name = column[Option[String]]("name")
    def email = column[Option[String]]("email")
    def avatarUrl = column[Option[String]]("avatarUrl")

    def reg_lang = column[Lang]("reg_lang")
    def reg_ip = column[String]("reg_ip")
    def reg_host = column[Option[String]]("reg_host")
    def reg_userAgent = column[Option[String]]("reg_userAgent")
    def reg_activated = column[Boolean]("reg_activated")
    def reg_dateTime = column[Instant]("reg_dateTime")

    def lang = column[Lang]("lang")
    def timeZone = column[Option[String]]("timeZone")

    def * = (id, name, email, avatarUrl, reg_lang, reg_ip, reg_host, reg_userAgent, reg_activated, reg_dateTime, lang, timeZone) <> (UserRow.tupled, UserRow.unapply)
  }

  /**
   * The LoginInfoRow object.
   *
   * @param id           The ID of the login info. Exists so that passwords, oauth tokens, and others can point back.
   * @param userId       The ID of the UserRow.
   * @param providerId   The ID of the provider.
   * @param providerKey  The provider auth key.
   */
  case class LoginInfoRow(id: Option[UUID], userId: UUID, providerId: String, providerKey: String)

  class LoginInfos(tag: Tag) extends Table[LoginInfoRow](tag, "user_login_info") {
    def id = column[UUID]("id", O.PrimaryKey)
    def userId = column[UUID]("userId")
    def providerId = column[String]("providerId")
    def providerKey = column[String]("providerKey")

    def user = foreignKey("ID_FK", userId, usersT)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def * = (id.?, userId, providerId, providerKey) <> (LoginInfoRow.tupled, LoginInfoRow.unapply)
  }

  /**
   * The PasswordInfoRow object.
   *
   * @param loginInfoId  The owning LoginInfo
   * @param hasher       The Hasher used.
   * @param password     The hashed password.
   * @param salt         The salt added to the password pre-hash.
   */
  case class PasswordInfoRow(loginInfoId: Option[UUID], hasher: String, password: String, salt: Option[String] = None)

  class PasswordInfos(tag: Tag) extends Table[PasswordInfoRow](tag, "user_passwords") {
    def loginInfoId = column[UUID]("loginId", O.PrimaryKey)
    def hasher = column[String]("hasher")
    def password = column[String]("password")
    def salt = column[Option[String]]("salt")

    def loginInfo = foreignKey("LOGIN_ID_FK", loginInfoId, loginInfoT)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def * = (loginInfoId.?, hasher, password, salt) <> (PasswordInfoRow.tupled, PasswordInfoRow.unapply)
  }

  val authTokensT = TableQuery[AuthTokens]
  val usersT = TableQuery[Users]
  val loginInfoT = TableQuery[LoginInfos]
  val passwordInfoT = TableQuery[PasswordInfos]

  val schema = authTokensT.schema ++ usersT.schema ++ loginInfoT.schema ++ passwordInfoT.schema
  val create = DBIO.seq(schema.create)
  val drop = DBIO.seq(schema.drop)
}
// scalastyle:on