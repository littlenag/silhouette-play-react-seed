import sbt._

object Dependencies {

  object Version {
    val slick = "3.2.3"
    val specs2 = "3.8.9"
    val silhouette = "5.0.1"
    val akka = "2.5.4"
  }

  val resolvers = Seq(
    Resolver.jcenterRepo
  )

  object Library {
    object Play {
      val version: String = play.core.PlayVersion.current
      val ws: ModuleID = "com.typesafe.play" %% "play-ws" % version
      val cache: ModuleID = "com.typesafe.play" %% "play-cache" % version
      val test: ModuleID = "com.typesafe.play" %% "play-test" % version
      val specs2: ModuleID = "com.typesafe.play" %% "play-specs2" % version
      //val scalatest: ModuleID = "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2"
    }

    object Specs2 {
      val core: ModuleID = "org.specs2" %% "specs2-core" % Version.specs2
      val matcherExtra: ModuleID = "org.specs2" %% "specs2-matcher-extra" % Version.specs2
      val mock: ModuleID = "org.specs2" %% "specs2-mock" % Version.specs2
    }

    object Silhouette {
      val core: ModuleID = "com.mohiva" %% "play-silhouette" % Version.silhouette
      val passwordBcrypt: ModuleID = "com.mohiva" %% "play-silhouette-password-bcrypt" % Version.silhouette
      val persistence: ModuleID = "com.mohiva" %% "play-silhouette-persistence" % Version.silhouette
      val cryptoJca: ModuleID = "com.mohiva" %% "play-silhouette-crypto-jca" % Version.silhouette
      val testkit: ModuleID = "com.mohiva" %% "play-silhouette-testkit" % Version.silhouette
    }

    object Slick {
      val slick = "com.typesafe.slick" %% "slick" % Version.slick
      val slickHikaricp = "com.typesafe.slick" %% "slick-hikaricp" % Version.slick
      val hikaricp = "com.zaxxer" % "HikariCP" % "2.6.3"
      val jodaMapper = "com.github.tototoshi" %% "slick-joda-mapper" % "2.3.0"
      val joda = "joda-time" % "joda-time" % "2.9.9"
      val jodaConvert = "org.joda" % "joda-convert" % "1.7"
      val postgresql = "org.postgresql" % "postgresql" % "42.2.2"
      val h2 = "com.h2database" % "h2" % "1.4.197" // % Test
      val flyway = "org.flywaydb" % "flyway-core" % "5.1.1"
      val slickMigration = "io.github.nafg" %% "slick-migration-api" % "0.4.2"
      val slickMigrationFlyway = "com.1on1development" %% "slick-migration-api-flyway" % "0.5.0-SNAPSHOT"
    }

    object Akka {
      val testkit: ModuleID = "com.typesafe.akka" %% "akka-testkit" % Version.akka
    }

    val ficus: ModuleID = "com.iheart" %% "ficus" % "1.4.2"
    val scalaGuice: ModuleID = "net.codingwell" %% "scala-guice" % "4.2.1"
    val akkaQuartzScheduler: ModuleID = "com.enragedginger" %% "akka-quartz-scheduler" % "1.6.1-akka-2.5.x"
    val playMailer: ModuleID = "com.typesafe.play" %% "play-mailer" % "6.0.1"
    val playMailerGuice: ModuleID = "com.typesafe.play" %% "play-mailer-guice" % "6.0.1"
    val apacheCommonsIO: ModuleID = "commons-io" % "commons-io" % "2.4"
  }
}
