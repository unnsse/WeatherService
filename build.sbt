lazy val http4sVersion = "0.23.28"
lazy val circeVersion = "0.14.5"
lazy val sttpVersion = "3.8.3"

ThisBuild / scalaVersion := "2.13.14"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-ember-server" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "com.softwaremill.sttp.client3" %% "core" % sttpVersion,
  "com.softwaremill.sttp.client3" %% "circe" % sttpVersion,
  "com.comcast" %% "ip4s-core" % "3.0.1",
  "org.typelevel" %% "log4cats-slf4j" % "2.6.0",
  "ch.qos.logback" % "logback-classic" % "1.4.7",
  
  // Test dependencies
  "org.scalatest" %% "scalatest" % "3.2.15" % Test,
  "org.typelevel" %% "cats-effect-testing-scalatest" % "1.5.0" % Test,
  "org.http4s" %% "http4s-client" % http4sVersion % Test
)

Compile / run / fork := true

// Add this line to enable parallel execution of tests
Test / parallelExecution := true
