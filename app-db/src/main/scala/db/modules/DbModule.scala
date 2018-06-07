package db.modules

import com.google.inject.{ Provides, Singleton }
import com.typesafe.config.Config
import db.utils.{ MigrationAssistant, SlickSession }
import net.codingwell.scalaguice.ScalaModule

/**
 * The Guice `Db` module.
 */
class DbModule extends ScalaModule {

  /**
   * Configures the module.
   */
  override def configure(): Unit = {

  }

  /**
   * Provides the SlickSession.
   *
   * @param config Application configuration
   * @return The SlickSession implementation.
   */
  @Provides
  @Singleton
  def provideSession(config: Config): SlickSession = {
    SlickSession.forConfig("app.database", config)
  }

  /**
   * Provides the MigrationAssistant.
   *
   * @param slickSession The SlickSession implementation.
   * @return The MigrationAssistant.
   */
  @Provides
  @Singleton
  def provideMigrationAssistant(slickSession: SlickSession): MigrationAssistant = {
    new MigrationAssistant()(slickSession)
  }

}
