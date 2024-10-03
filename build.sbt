lazy val http4sVersion = "0.23.28"
lazy val circeVersion = "0.14.9"
lazy val sttpVersion = "3.9.7"
lazy val ips4sCoreVersion = "3.6.0"
lazy val log4CatsCoreVersion = "2.7.0"
lazy val log4CatsSlf4jVersion = "2.7.0"
lazy val logbackClassicVersion = "1.5.6"
lazy val scalaTestVersion = "3.2.19"
lazy val catsEffectTestingScalaTestVersion = "1.5.0"

ThisBuild / scalaVersion := "2.13.14"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-ember-server" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "com.softwaremill.sttp.client3" %% "core" % sttpVersion,
  "com.softwaremill.sttp.client3" %% "circe" % sttpVersion,
  "com.comcast" %% "ip4s-core" % ips4sCoreVersion,
  "org.typelevel" %% "log4cats-core" % log4CatsCoreVersion,
  "org.typelevel" %% "log4cats-slf4j" % log4CatsSlf4jVersion,
  "ch.qos.logback" % "logback-classic" % logbackClassicVersion,
  // Test dependencies
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
  "org.typelevel" %% "cats-effect-testing-scalatest" % catsEffectTestingScalaTestVersion % Test,
  "org.http4s" %% "http4s-client" % http4sVersion % Test
)

Compile / run / fork := true

// Add this line to enable parallel execution of tests
Test / parallelExecution := true
