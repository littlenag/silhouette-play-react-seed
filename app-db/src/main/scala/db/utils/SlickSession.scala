package db.utils

import com.typesafe.config.{ Config, ConfigFactory }
import slick.basic.DatabaseConfig
import slick.jdbc.{ JdbcBackend, JdbcProfile }

// Inspired by https://developer.lightbend.com/docs/api/alpakka/0.18/akka/stream/alpakka/slick/javadsl/SlickSession$.html

/**
 * Represents an "open" Slick database and its database (type) profile.
 *
 * <b>NOTE</b>: these databases need to be closed after creation to
 * avoid leaking database resources like active connection pools, etc.
 */
sealed abstract class SlickSession {
  val db: JdbcBackend#Database
  val profile: JdbcProfile

  //lazy val joda = new GenericJodaSupport(profile)

  /**
   * You are responsible for closing the database after use!!
   */
  def close(): Unit = db.close()
}

/**
 * Methods for "opening" Slick databases for use.
 *
 * <b>NOTE</b>: databases created through these methods will need to be
 * closed after creation to avoid leaking database resources like active
 * connection pools, etc.
 */
object SlickSession {
  private final class SlickSessionImpl(val slick: DatabaseConfig[JdbcProfile]) extends SlickSession {
    val db: JdbcBackend#Database = slick.db
    val profile: JdbcProfile = slick.profile
  }

  def forConfig(path: String): SlickSession = forConfig(path, ConfigFactory.load())
  def forConfig(config: Config): SlickSession = forConfig("", config)
  def forConfig(path: String, config: Config): SlickSession = forConfig(
    DatabaseConfig.forConfig[JdbcProfile](path, config)
  )
  def forConfig(databaseConfig: DatabaseConfig[JdbcProfile]): SlickSession = new SlickSessionImpl(databaseConfig)
}
