package auth.utils.json

import auth.models.{ Registration, Settings, User }
import com.mohiva.play.silhouette.api.util.PasswordInfo
import play.api.libs.json.{ Json, OFormat }

/**
 * Implicit JSON formats.
 */
trait Formats extends core.utils.json.Formats {

  /**
   * Converts a [[PasswordInfo]] instance to JSON and vice versa.
   */
  implicit val passwordInfoFormat: OFormat[PasswordInfo] = Json.format

  /**
   * Converts a [[Settings]] instance to JSON and vice versa.
   */
  implicit val settingsFormat: OFormat[Settings] = Json.format
}

/**
 * API centric JSON formats.
 */
object APIFormats extends core.utils.json.APIFormats with Formats {

  /**
   * Converts a [[Registration]] instance to JSON and vice versa.
   */
  implicit val registrationFormat: OFormat[Registration] = Json.format

  /**
   * Converts a [[User]] instance to JSON and vice versa.
   */
  implicit val userFormat: OFormat[User] = Json.format
}
