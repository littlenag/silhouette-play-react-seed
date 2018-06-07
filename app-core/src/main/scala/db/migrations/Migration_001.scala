package db.migrations

import db.utils.{ SlickSession, Tables }
import slick.jdbc.JdbcProfile
import slick.migration.api.{ Dialect, GenericDialect, TableMigration }
import slick.migration.api.flyway.VersionedMigration

/**
 *
 */
object Migration_001 {

  def apply()(implicit session: SlickSession): VersionedMigration = {

    val tables: Tables = Tables()
    import tables._

    implicit val dialect: Dialect[_ <: JdbcProfile] = GenericDialect(session.profile)

    val authTokens =
      TableMigration(authTokensT)
        .create
        .addColumns(_.id, _.userId, _.expiry)

    val users =
      TableMigration(usersT)
        .create
        .addColumns(_.id, _.name, _.email, _.avatarUrl)
        .addColumns(_.reg_lang, _.reg_ip, _.reg_host, _.reg_userAgent, _.reg_activated, _.reg_dateTime)
        .addColumns(_.lang, _.timeZone)

    val loginInfo =
      TableMigration(loginInfoT)
        .create
        .addColumns(_.id, _.userId, _.providerId, _.providerKey)
        .addForeignKeys(_.user)

    val passwords =
      TableMigration(passwordInfoT)
        .create
        .addColumns(_.loginInfoId, _.hasher, _.password, _.salt)
        .addForeignKeys(_.loginInfo)

    VersionedMigration(1, authTokens & users & loginInfo & passwords)
  }

}
