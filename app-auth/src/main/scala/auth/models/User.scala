package auth.models

import java.util.UUID

import com.mohiva.play.silhouette.api.{ Identity, LoginInfo }

/**
 * The user object.
 *
 * Note: a User may have multiple LoginInfos associated with them!
 *
 * @param id           The unique ID of the user.
 * @param loginInfo    The login used.
 * @param name         Maybe the name of the authenticated user.
 * @param email        Maybe the email of the authenticated provider.
 * @param avatarURL    Maybe the avatar URL of the authenticated provider.
 * @param registration The registration data.
 * @param settings     The user settings.
 */
case class User(
  id: UUID,
  loginInfo: LoginInfo,
  name: Option[String],
  email: Option[String],
  avatarURL: Option[String],
  registration: Registration,
  settings: Settings
) extends Identity