import Dependencies.Library
import play.sbt.{ PlayLayoutPlugin, PlayScala }
import sbt._

////*******************************
//// Core module
////*******************************
lazy val core: Project = Project(id = "app-core", base = file("app-core"))
  .settings(
    libraryDependencies ++= Seq(
      // Shared regular components
      Library.scalaGuice,
      Library.apacheCommonsIO,
      Library.Slick.slick,
      Library.Slick.slickHikaricp,
      Library.Slick.flyway,
      Library.Slick.slickMigration,
      Library.Slick.slickMigrationFlyway,
      Library.Slick.h2,
      Library.Slick.postgresql,

      // Shared Test components
      Library.Play.test % Test,
      Library.Play.specs2 % Test,
      Library.Akka.testkit % Test,
      Library.Specs2.matcherExtra % Test,
      filters % Test
    )
  )
  .enablePlugins(PlayScala, DisablePackageSettings)
  .disablePlugins(PlayLayoutPlugin)

////*******************************
//// Auth module
////*******************************
lazy val auth: Project = Project(id = "app-auth", base = file("app-auth"))
  .dependsOn(core % "compile->compile;test->test")
  .settings(
    libraryDependencies ++= Seq(
      Library.Silhouette.core,
      Library.Silhouette.passwordBcrypt,
      Library.Silhouette.persistence,
      Library.Silhouette.cryptoJca,
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
lazy val admin: Project = Project(id = "app-admin", base = file("app-admin"))
  .dependsOn(core % "compile->compile;test->test", auth % "compile->compile;test->test")
  .enablePlugins(PlayScala, DisablePackageSettings)
  .disablePlugins(PlayLayoutPlugin)

////*******************************
//// Root module
////*******************************
lazy val root: Project = Project(id = "silhouette-play-react-seed", base = file("."))
  .aggregate(core, auth, admin)
  .dependsOn(auth, admin)
  .settings(
    libraryDependencies ++= Seq(
      filters
    )
  )
  .enablePlugins(PlayScala, NpmSettings, PackageSettings)
  .disablePlugins(PlayLayoutPlugin)
