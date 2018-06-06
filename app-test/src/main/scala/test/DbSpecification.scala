package test

import java.io.InputStream
import java.nio.file.Paths

import db.utils.{ SlickSession, Tables }
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.{ PlaySpecification, WithApplication }
import play.api.{ Environment, Logger }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }
import scala.io.Source

/**
 * A custom specification which starts a H2 instance before all the tests, and stops it after all of them.
 *
 * Note: This is handled like a global setup/teardown procedure. So you must clean the database after each test,
 * to get an isolated test case.
 */
trait DbSpecification extends PlaySpecification { self =>
  sequential

  import play.api.Logger

  lazy val tables = new {
    val session: SlickSession = SlickSession.forConfig("app.database")
  } with Tables

  /**
   * Runs a fake application with a test database.
   */
  class WithDb(applicationBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder())
    extends WithApplication(
      applicationBuilder
        .bindings(bind(classOf[SlickSession]).to(tables.session))
        .build()
    )

  /**
   * The MongoDB scope.
   */
  trait DbScope extends BeforeAfterWithinAround {
    self: WithApplication =>

    /**
     * Names of SQL files to exec.
     */
    val fixtures: Seq[String] = Seq()

    /**
     * The ReactiveMongo API.
     */
    lazy val session = app.injector.instanceOf[SlickSession]

    /**
     * The application environment.
     */
    implicit val env = app.injector.instanceOf[Environment]

    /**
     * Run each of the SQL files line by line.
     */
    def before: Unit = {
      Logger.info("BEFORE BEFORE BEFORE BEFORE BEFORE BEFORE")
      val runStatementsF = for {
        _ <- Future.successful(Logger.info(tables.schema.createStatements.mkString("\n")))
        _ <- session.db.run(tables.create)
        _ <- Future.successful(Logger.info("Tables created"))
        st = session.db.createSession().createStatement()
        _ <- Future {
          fixtures.foreach { sqlFile =>
            Logger.info(s"File: $sqlFile")
            Source.fromInputStream(getClass.getResourceAsStream(s"/$sqlFile")).getLines.foreach { line =>
              Logger.info(s"SQL: $line")
              st.execute(line)
            }
          }
        }
      } yield ()

      Await.result(runStatementsF, Duration(60, SECONDS))
    }

    /**
     * Drops the database after the test runs to get an isolated environment.
     */
    def after: Unit = {
      Logger.info("AFTER AFTER AFTER AFTER AFTER AFTER AFTER")
      Logger.info(tables.schema.dropStatements.mkString("\n"))
      Await.result(session.db.run(tables.drop), Duration(60, SECONDS))
    }
  }

}
