import Dependencies.Library
import play.sbt.{ PlayLayoutPlugin, PlayScala }
import sbt._

////*******************************
//// Common module
////*******************************
val database: Project = Project(id = "app-db", base = file("app-db"))
  .settings(
    libraryDependencies ++= Seq(
      Library.scalaGuice,
      Library.Slick.slick,
      Library.Slick.slickHikaricp,
      Library.Slick.flyway,
      Library.Slick.slickMigration,
      Library.Slick.slickMigrationFlyway,
      Library.Slick.h2,
      Library.Slick.postgresql
    )
  )
  .enablePlugins(PlayScala, DisablePackageSettings)
  .disablePlugins(PlayLayoutPlugin)

////*******************************
//// Test module
////*******************************
val test: Project = Project(id = "app-test", base = file("app-test"))
  .dependsOn(database)
  .settings(
    libraryDependencies ++= Seq(
      Library.Play.test,
      Library.Play.specs2,
      Library.Akka.testkit,
      Library.Specs2.matcherExtra,
      Library.scalaGuice,
      Library.Slick.slick,
      Library.Slick.h2,
      filters
    )
  )

////*******************************
//// Core module
////*******************************
val core: Project = Project(id = "app-core", base = file("app-core"))
  .dependsOn(database, test % Test)
  .settings(
    libraryDependencies ++= Seq(
      Library.scalaGuice,
      Library.apacheCommonsIO,
      Library.Slick.slick,
      Library.Slick.slickHikaricp
    )
  )
  .enablePlugins(PlayScala, DisablePackageSettings)
  .disablePlugins(PlayLayoutPlugin)

////*******************************
//// Auth module
////*******************************
val auth: Project = Project(id = "app-auth", base = file("app-auth"))
  .dependsOn(database, core, test % Test)
  .settings(
    libraryDependencies ++= Seq(
      Library.Silhouette.core,
      Library.Silhouette.passwordBcrypt,
      Library.Silhouette.persistence,
      Library.Silhouette.cryptoJca,
      Library.Slick.slick,
      Library.Slick.h2,
      Library.Slick.postgresql,
      Library.Slick.slickHikaricp,
      Library.Slick.jodaMapper,
      Library.scalaGuice,
      Library.ficus,
      Library.playMailer,
      Library.playMailerGuice,
      Library.akkaQuartzScheduler,
      Library.Silhouette.testkit % Test,
      Library.Specs2.matcherExtra % Test,
      Library.Akka.testkit % Test,
      ws,
      guice,
      specs2 % Test
    )
  )
  .enablePlugins(PlayScala, DisablePackageSettings)
  .disablePlugins(PlayLayoutPlugin)

////*******************************
//// Admin module
////*******************************
val admin: Project = Project(id = "app-admin", base = file("app-admin"))
  .dependsOn(auth % "compile->compile;test->test", test % Test)
  .enablePlugins(PlayScala, DisablePackageSettings)
  .disablePlugins(PlayLayoutPlugin)

////*******************************
//// Root module
////*******************************
val root: Project = Project(id = "silhouette-play-react-seed", base = file("."))
  .aggregate(database, test, core, auth, admin)
  .dependsOn(auth, admin)
  .settings(
    libraryDependencies ++= Seq(
      filters
    )
  )
  .enablePlugins(PlayScala, NpmSettings, PackageSettings)
  .disablePlugins(PlayLayoutPlugin)
