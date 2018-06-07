package db.utils

import java.io.PrintWriter
import java.sql.Connection
import java.util.logging.Logger

import javax.sql.DataSource
import org.flywaydb.core.Flyway
import slick.jdbc.JdbcDataSource
import slick.migration.api.flyway.Resolver

import scala.languageFeature.implicitConversions

/**
 *
 */
class MigrationAssistant(implicit slickSession: SlickSession) {

  lazy val flyway: Flyway = {
    val flyway = new Flyway
    flyway.setDataSource(slickSession.db.source)
    flyway.setLocations()
    flyway
  }

  implicit class AdaptJdbcDataSource(jdbcDataSource: JdbcDataSource) extends DataSource {
    override def getConnection: Connection = jdbcDataSource.createConnection()
    override def getConnection(username: String, password: String): Connection = getConnection
    override def getLogWriter: PrintWriter = ???
    override def setLogWriter(out: PrintWriter): Unit = ???
    override def setLoginTimeout(seconds: Int): Unit = ???
    override def getLoginTimeout: Int = ???
    override def getParentLogger: Logger = ???
    override def unwrap[T](iface: Class[T]): T = ???
    override def isWrapperFor(iface: Class[_]): Boolean = ???
  }

  def migrate(): Unit = {
    import db.migrations._

    flyway.setResolvers(Resolver(Migration_001()))

    flyway.migrate()

  }

  def clean(): Unit = {
    flyway.clean()
  }

  def validate(): Unit = {
    flyway.validate()
  }

}
