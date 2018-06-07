package test

import db.modules.DbModule
import db.utils.{ MigrationAssistant, SlickSession, Tables }
import org.specs2.specification.core.Fragments
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.{ PlaySpecification, WithApplication }
import play.api.Environment
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

/**
 * A custom specification which starts a H2 instance before all the tests, and stops it after all of them.
 *
 * Note: This is handled like a global setup/teardown procedure. So you must clean the database after each test,
 * to get an isolated test case.
 */
trait DbSpecification extends PlaySpecification { self =>
  sequential

  override def map(fs: => Fragments): Fragments = step(start()) ^ fs ^ step(stop())

  import play.api.Logger

  /**
   * Runs a fake application with a test database.
   */
  class WithDb(applicationBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder())
    extends WithApplication(
      applicationBuilder
        .overrides(new DbModule)
        .build()
    )

  /**
   * The DB scope.
   */
  trait DbScope extends BeforeAfterWithinAround {
    self: WithApplication =>

    /**
     * SQL actions to exec.
     */
    val actions: DBIO[_] = DBIO.successful(())

    /**
     * The SlickSession.
     */
    lazy val session = app.injector.instanceOf[SlickSession]

    /**
     * The MigrationAssistant.
     */
    lazy val migrationAssistant = app.injector.instanceOf[MigrationAssistant]

    /**
     * The application environment.
     */
    implicit val env = app.injector.instanceOf[Environment]

    /**
     * Run each of the SQL files line by line.
     */
    def before: Unit = {
      try {
        val runStatementsF = for {
          _ <- Future { migrationAssistant.migrate() }
          _ <- session.db.run(actions)
        } yield ()

        Await.result(runStatementsF, Duration(60, SECONDS))
      } catch {
        case ex: Exception =>
          Logger.error("DbScope - error processing before()", ex)
      }
    }

    /**
     * Drops the database after the test runs to get an isolated environment.
     */
    def after: Unit = {
      try {
        migrationAssistant.clean()
        session.close()
      } catch {
        case ex: Exception =>
          Logger.error("DbScope - error processing after()", ex)
      }
    }
  }

  /**
   * Start everything.
   */
  private def start(): Unit = {

  }

  /**
   * Stop everything.
   */
  private def stop(): Unit = {

  }

}
